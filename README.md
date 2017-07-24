# demo
Spring-boot-rest-api-postgre-docker-spock

## Build and run instructions
1. Install Java 8 JDK
2. Install [Docker Toolbox](https://www.docker.com/products/docker-toolbox)
3. Set `DOCKER_IP` environment variables (see Notes section)
4. Run a full build with:
 ```
 mvnw clean install
 ```
5. Start PostreSQL and the demo application by running:
 ```
 docker-compose up -d
 ```
6. Access the application swagger on
 ```
http://192.168.99.100:18884/swagger-ui.html
 ```
(This considers your docker machine is running on 192.168.99.100 as default)


## Notes

To set the `DOCKER_IP` environment variable:

* Windows
```
FOR /f "tokens=*" %i IN ('docker-machine env') DO @%i

```
You can also run docker-machine env and manually put these environment variables in windows advanced config
* Linux
```
export DOCKER_IP=127.0.0.1

```
* Mac
```
export DOCKER_IP=`docker-machine ip`
```

## Bigger docker VM
Sometimes you will need a bigger docker VM. Clear out the default one and recreate with the command below.

* Windows
```
docker-machine create --driver 'virtualbox' --virtualbox-cpu-count='2' --virtualbox-disk-size='16000' --virtualbox-memory='4096' --virtualbox-hostonly-cidr '192.168.59.3/24' default && eval $(docker-machine env default) && docker-compose up -d
```

## Summary of system variables
Tested on windows. These are the system variables that should have set along the way for things to work smoothly.

* Windows
Control Panel > All Control Panel Items > System > Advance System Settings > Environment Variables
```
  C:\Users\<YOUR USERNAME>\.m2
  JAVA_HOME = <PATH TO ROOT OF YOUR JDK>
  COMPOSE_CONVERT_WINDOWS_PATHS = true
  DOCKER_CERT_PATH = <FROM YOUR docker-machine env>
  DOCKER_HOST = <FROM YOUR docker-machine env>
  DOCKER_IP = <FROM YOUR docker-machine ip>
  DOCKER_MACHINE_NAME = default //can be changed see bigger docker vm section
  DOCKER_TLS_VERIFY=1
```