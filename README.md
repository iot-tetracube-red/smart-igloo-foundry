# Smart Igloo Foundry

Smart Igloo Hub is made in Quarkus and is written having scalability and high availability in mind.
These requirements are not requiring necessarily a powerful hardware, instead you can run the hub in
a kubernetes installation in a RaspberryPi 4 where are hosted also other services (see PiHole).

## Pre-requirements

You need a valid kubernetes installation (on cloud or on your local network), a folder where is 
possible to put data files and authorizations to operate in kubernetes cluster itself.

## Kubernetes preparation

If you want to develop or deploy production environment of the application server you need to prepare
the kubernetes configurations and secrets to store in the cluster itself.

### Namespace creation

The namespace is essential to isolate the services in the kubernetes cluster. For this reason
in first instance you need to deploy the namespace launching:
```shell
kubectl apply -f namespace/smart-igloo-namespace.yaml
```

### Database deployment

The database needs secrets to protect unwanted access from external applications. For this reason
is essential to create default username and password to access and use the database itself.
To archive this you need to create the credentials by typing this command to create username:
```shell
echo -n '<insert your password here>' > ./db_password
```

Then is possible to store secrets in kubernetes cluster referencing the right namespace:
```shell
kubectl create secret generic igloo-db-storage-secrets -n smart-igloo-hub \
  --from-file=./db_password
```

You can optionally remove previously created files:
```shell
rm -v ./db_password
```

Now is time to prepare a folder where persist database's data files. This folder is used
by kubernetes to persist database's date over deployments and upgrades. So in this way is possible
upgrade, change or remove or scale database without loosing data stored in. 

To archive this, identify a folder in your system where you can create (as normal user) a folder
and run the script *prepare_db_storage.py*:
```shell
python ./deployments/kuberenetes/prepare_db_storage.py <base_deployment_path>
```
In the file **smart-igloo-database-deployment** open both files **persistent-volume-data.yaml**
and **persistent-volume-db-init.yaml** and change the variable `{db_data_path}` 
with the path that you choose as base path when you launched the script of storage preparation above.

Once the variables are updates, you can launch the deployment of the persistent volumes:
```shell
kubectl apply -f smart-igloo-database-deployment/db-data-persistent-volume.yaml \
    -f smart-igloo-database-deployment/db-init-persistent-volume.yaml \
    -f smart-igloo-database-deployment/db-data-persistent-volume-claim.yaml \
    -f smart-igloo-database-deployment/db-init-persistent-volume-claim.yaml \
    -n smart-igloo-hub
```

Now is possible deploy the PostgreSQL itself simply applying the deployment file:
```shell
kubectl apply -f smart-igloo-database-deployment/database-deployment.yaml -n smart-igloo-hub
```

The PostgreSQL needs to expose its services inside the Kubernetes cluster to allow other
services to interact with it. Apply the service file for internal ports exposure:
```shell
kubectl apply -f smart-igloo-database-deployment/cluser-ip-service.yaml -n smart-igloo-hub
```

If you are developing, or you want to inspect database outside the cluster, you also need to 
deploy the load balancer service to expose PostgreSQL ports externally the cluster:
```shell
kubectl apply -f smart-igloo-database-deployment/load-balancer-service.yaml -n smart-igloo-hub
```