# Smart Igloo Foundry

Smart Igloo Hub is made in Quarkus and is written having scalability and high availability in mind.
These requirements are not requiring necessarily a powerful hardware, instead you can run the hub in
a kubernetes installation in a RaspberryPi 4 where are hosted also other services (see PiHole).

## Pre-requirements

You need a valid kubernetes installation (on cloud or on your local network), a folder where is 
possible to put data files and authorizations to operate in kubernetes cluster itself.

## Run the CLI

The CLI requires some options to configure properly the services and configurations:

To interact with your cluster you need to specify the host of kubernetes, use the option
**--k8s-host** to specify where your kuberenetes cluster is hosted, it's default value is
*http://localhost:8080*.


