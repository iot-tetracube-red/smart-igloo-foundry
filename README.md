# Smart Igloo Foundry

Smart Igloo Hub is made in Quarkus and is written having scalability and high availability in mind.
These requirements are not requiring necessarily a powerful hardware, instead you can run the hub in
a kubernetes installation in a RaspberryPi 4 where are hosted also other services (see PiHole).

## Pre-requirements

You need a valid kubernetes installation (on cloud or on your local network), a folder where is
possible to put data files and authorizations to operate in kubernetes cluster itself.

## The CLI

Environment of deployment: 
* *dev* - development: is kind of development suggested to users that want to develop or test on local
environment the Smart Igloo Hub platform, then some services will be exposed on the bare metal machine
* *prod* - production: will deploy all service in kubernetes and all services are exposed internally 
and only mqtt port will be exposed externally to allow appliances to connect with the hub


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