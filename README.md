# Bindaas Data Integration Middleware Platform

## Building Bindaas

Bindaas can be built using Apache Maven 3.x and Java 1.7.x or higher.

 $ mvn clean install

Bindaas binaries can be found in the binaries folder.

Built and tested with Apache Maven 3.1.1 to 3.5.4 and Oracle Java 1.7 to 1.8.

Bindaas does not build with JDK 10, due to incompatibilities in Maven plugins.


## Executing Bindaas

Once you have successfully built Bindaas, you will find the binary in a compressed format, as in
bindaas-dist-2.0.7-201806281144-min.tar.gz


Extract this compressed binary. Now bindaas-dist-2.0.7-201806281144-min will be your <BINDAAS-BINARY-HOME>.

Bindaas can be executed using the startup.sh script in
<BINDAAS-BINARY-HOME>/bin

$ sh startup.sh

Similarly, you may shut down Bindaas by executing the shutdown script:

$ sh shutdown.sh

If you would like to have an interactive OSGi console with logs displayed inline:

$ java -Dpid=BINDAAS_INSTANCE -Xmx1024m -jar org.eclipse.osgi_3.8.2.v20130124-134944.jar -console


## Troubleshooting

If you encounter the error "java.net.SocketException: Can't assign requested address" followed by a few
CXF errors when you are on a Mac with a wireless connection, please use the flag "-Djava.net.preferIPv4Stack=true" to fix this,
as shown below:

$ java -Djava.net.preferIPv4Stack=true -Dpid=BINDAAS_INSTANCE -Xmx1024m -jar org.eclipse.osgi_3.8.2.v20130124-134944.jar -console


## Verify Setup

To verify everything is setup correctly visit the following url in your web-browser :
http://localhost:8080/dashboard/

You should be greeted by Bindaas' Login page.

The logs can be found at <BINDAAS-BINARY-HOME>/log/bindaas.log.


## Log in to Bindaas Management Console

Log in using the default,
user name: admin
password: password



## Configuring with Kong API Gateway using the Docker containers

### Configure Kong with Postgres and Apache DS

First get the Sharmalab's kong-ldap repository:

$ git clone https://github.com/sharmalab/kong-ldap.git

Now run the buildRun script:

$ cd kong-ldap

$ sh buildRun.sh


### Configure Bindaas with Kong
To configure the services:

$ curl -i -X POST   --url http://127.0.0.1:8001/apis/   --data 'name=bindaxy'   --data 'hosts=bindaxy.com'  --data 'upstream_url=http://docker.for.mac.host.internal:9099'

$ curl -i -X GET   --url http://127.0.0.1:8000/services/test/mongo/query/find?api_key=d9076d81-147d-44c3-9af6-d3dc5d9f204b   --header 'Host: bindaxy.com'


To configure the dashboard:

$ curl -i -X POST   --url http://127.0.0.1:8001/apis/   --data 'name=bindax'   --data 'hosts=bindax.com'  --data 'upstream_url=http://docker.for.mac.host.internal:8080'

$ curl -i -X GET   --url http://127.0.0.1:8000/dashboard/   --header 'Host: bindax.com'


The above commands are for Docker for Mac.

Replace "docker.for.mac.host.internal" in the above commands with "host.docker.internal" for Docker environments other than "Docker for Mac"


If you prefer to run Bindaas using its Docker container, please refer to docker/README.md for more details.


### How to change the admin dashboard port from 8080 to something else?

You have two options.

i) Pass the new port as an argument:

java -Dorg.osgi.service.http.port=8082 -Djava.net.preferIPv4Stack=true -Dpid=BINDAAS_INSTANCE -Xmx1024m -jar org.eclipse.osgi_3.8.2.v20130124-134944.jar -console

ii) Change the below line in bin/config.ini from 8080 to something else:

org.osgi.service.http.port=8081

If both (i) and (ii) are configured, the input in the console argument takes precedence. 
i.e., the dashboard uses the port 8082 above.

