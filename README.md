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

### Database deployment



Now is time to prepare a folder where persist database's data files. This folder is used
by kubernetes to persist database's date over deployments and upgrades. So in this way is possible
upgrade, change or remove or scale database without loosing data stored in.

To archive this, identify a folder in your system where you can create (as normal user) a folder
and run the script *prepare_db_storage.py*:

```shell
python ./prepare_db_storage.py <base_deployment_path>
```

In the file **smart-igloo-database-deployment** open both files **db-data-persistent-volume.yaml**
and **db-init-persistent-volume.yaml** and change the variable `{db_data_path}`
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

### Message broker deployment

Open the file **smart-igloo-message-broker/config-map.yaml** and replace the url placeholder
**{smart_igloo_url}**. The placeholder must be substituted with these values according the
options:

* you are running the smart igloo hub service outside the cluster for some reason: the ip and port
  where the service is running;
* the service will be deployed inside the cluster: **smart-igloo-hub**

```shell
kubectl apply -f smart-igloo-message-broker/cluster-ip-service.yaml \
  -f smart-igloo-message-broker/load-balancer-service.yaml \
  -n smart-igloo-hub
```

In the end is possible to deploy the broker itself:
```shell
kubectl apply -f smart-igloo-message-broker/broker-deployment.yaml -n smart-igloo-hub
```

### Smart Igloo Hub deployment

Change the placeholder **{broker_host_url}** in file **smart-igloo-hub/config-map.yaml**.
The placeholder must be substituted with these values according the options:

* you are running the smart igloo hub service outside the cluster for some reason: then specify the ip of the
machine where is running the cluster (localhost if is same machine);
* the service will be deployed inside the cluster: set **smart-igloo-hub-broker** as option value

Then finally apply the config map:
```shell
kubectl apply -f smart-igloo-message-broker/config-map.yaml -n smart-igloo-hub
```

```shell
echo -n '<hub broker password>' > ./hub-broker-password
```

```shell
kubectl create secret generic smart-igloo-secrets -n smart-igloo-hub \
  --from-file=./db-password \
  --from-file=./hub-broker-password
```