##########################################
#  Configure SSH Tunneling to OKE Cluster
#########################################


echo "---------------------------------------------------------------"
echo "Configuring SSH Tunneling to OKE Cluster"
echo "---------------------------------------------------------------"

rm -rf ~/.ssh/config
rm -rf ~/.ssh/id_rsa*
rm -rf ~/.ssh/known_hosts
ssh-keygen -b 2048 -t rsa -f ~/.ssh/id_rsa -q -N ""
ssh-keyscan -H host.bastion.us-ashburn-1.oci.oraclecloud.com >> ~/.ssh/known_hosts


cat ~/.ssh/id_rsa.pub | tr -d '\n' > ~/.ssh/id_rsa_updated.pub
export ssh_public_key="~/.ssh/id_rsa_updated.pub"
export private_key="~/.ssh/id_rsa"
echo $private_key
echo $ssh_public_key

echo "Host host.bastion.us-ashburn-1.oci.oraclecloud.com" >> ~/.ssh/config
echo "    IdentityFile ~/.ssh/id_rsa" >> ~/.ssh/config
echo "    HostkeyAlgorithms +ssh-rsa" >> ~/.ssh/config
echo "    StrictHostKeyChecking no" >> ~/.ssh/config
echo "Host *" >> ~/.ssh/config
echo "    IdentityFile ~/.ssh/id_rsa" >> ~/.ssh/config
echo "    HostkeyAlgorithms +ssh-rsa" >> ~/.ssh/config
echo "    StrictHostKeyChecking no" >> ~/.ssh/config

export oci_bastion_ocid=$OCI_BASTION_OCID
export oci_cluster_ocid=$OCI_CLUSTER_OCID
echo $OCI_BASTION_OCID
echo $OCI_CLUSTER_OCID


##########################################
#  Create Bastion Session
#########################################


echo "---------------------------------------------------------------"
echo "Creating OCI Bastion Session"
echo "---------------------------------------------------------------"


export oke_cluster_private_ip=`oci ce cluster get --cluster-id $oci_cluster_ocid | grep -i -A 1 "private-endpoint" | grep -E -o "([0-9]{1,3}[\.]){3}[0-9]{1,3}"`

echo $oke_cluster_private_ip

echo "oci bastion session create-port-forwarding --bastion-id $oci_bastion_ocid --display-name github-to-oke-tunnel --ssh-public-key-file ~/.ssh/id_rsa_updated.pub --key-type PUB --target-private-ip $oke_cluster_private_ip --target-port 6443 --wait-for-state SUCCEEDED > ~/bastion_session.json"

oci bastion session create-port-forwarding --bastion-id $oci_bastion_ocid --display-name github-to-oke-tunnel --ssh-public-key-file ~/.ssh/id_rsa_updated.pub --key-type PUB --target-private-ip $oke_cluster_private_ip --target-port 6443 --wait-for-state SUCCEEDED > ~/bastion_session.json

export bastion_session_ocid=`cat ~/bastion_session.json | grep bastionsession | awk '{print $2}' | tail -1 | tr -d '"'`

echo $bastion_session_ocid

#################################################
#  Connect to OKE Cluster using SSH Tunneling 
#################################################

curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo chmod 755 kubectl
sudo mv kubectl /usr/local/bin

echo "---------------------------------------------------------------"
echo "Establishing SSH Connection to OKE Cluster through Bastion SSH Tunnel"
echo "---------------------------------------------------------------"

echo "ssh -i ~/.ssh/id_rsa -fN -L 6443:$oke_cluster_private_ip:6443 -p 22 $bastion_session_ocid@host.bastion.us-ashburn-1.oci.oraclecloud.com -v > ~/id_rsa.out"

ssh -i ~/.ssh/id_rsa -fN -L 6443:$oke_cluster_private_ip:6443 -p 22 $bastion_session_ocid@host.bastion.us-ashburn-1.oci.oraclecloud.com -v > ~/id_rsa.out

while [ $? != 0 ]
do
    echo "---------------------------------------------------------------"
    echo "Attempting again"
    echo "---------------------------------------------------------------"
    ssh -i ~/.ssh/id_rsa -fN -L 6443:$oke_cluster_private_ip:6443 -p 22 $bastion_session_ocid@host.bastion.us-ashburn-1.oci.oraclecloud.com -v > ~/id_rsa.out
done


echo "---------------------------------------------------------------"
echo "Configuring OKE Cluster"
echo "---------------------------------------------------------------"


rm -rf $HOME/.kube
mkdir $HOME/.kube
oci ce cluster create-kubeconfig --cluster-id $oci_cluster_ocid --file $HOME/.kube/config --region us-ashburn-1 --token-version 2.0.0  --kube-endpoint PRIVATE_ENDPOINT

sed -i "s|$oke_cluster_private_ip|127.0.0.1|g" $HOME/.kube/config
sleep 5
kubectl cluster-info
if [ $? == 0 ]
then
    echo "---------------------------------------------------------------"
    echo "Successfully Connected to your Private OKE Cluster"
    echo "---------------------------------------------------------------"
else 
    echo "---------------------------------------------------------------"
    echo "Unable to Authenticate with Private OKE Cluster"
    echo "---------------------------------------------------------------"
fi
