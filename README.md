# Bindaas Data Access Federation Middleware
<!---
[![Build Status](https://travis-ci.com/sharmalab/bindaas.svg?branch=master)](https://travis-ci.com/sharmalab/bindaas)
-->
Bindaas federates multiple data sources, provides access to them through REST interfaces, and enables query, access, and integration of data from diverse data sources such as MySQL and MongoDB.

## Building Bindaas

Bindaas can be built using Apache Maven 3.x and Java 1.8.

`$ mvn clean install`

Bindaas binaries can be found in the binaries folder.

Bindaas does not build with JDK 10, due to incompatibilities in Maven plugins.


## Executing Bindaas

Once you have successfully built Bindaas, you will find the binary in a compressed format in the binaries folder, as in
bindaas-dist-4.0.9-202208101418-min.tar.gz

The exact name of the binary changes based on the version and the times you have built, reflecting the major.minor versions.



Extract this compressed binary. 

`$ tar xvzf bindaas-dist-4.0.9-202208101418-min.tar.gz`

Now binaries/bindaas-dist-4.0.9-202208101418-min will be your BINDAAS-BINARY-HOME.

Bindaas can be executed using the startup.sh script in
BINDAAS-BINARY-HOME/bin

`$ sh startup.sh`

Similarly, you may shut down Bindaas by executing the shutdown script:

`$ sh shutdown.sh`

If you would like to have an interactive OSGi console with logs displayed inline:

`$ java -Dpid=BINDAAS_INSTANCE -Xmx1024m -jar org.eclipse.osgi_3.10.100.v20150529-1857.jar -console`


## Troubleshooting

If you encounter the error "java.net.SocketException: Can't assign requested address" followed by a few
CXF errors when you are on a Mac with a wireless connection, please use the flag "-Djava.net.preferIPv4Stack=true" to fix this,
as shown below:

`$ java -Djava.net.preferIPv4Stack=true -Dpid=BINDAAS_INSTANCE -Xmx1024m -jar org.eclipse.osgi_3.10.100.v20150529-1857.jar -console`


## Verify Setup

To verify everything is setup correctly visit the following url in your web-browser :
http://localhost:8080/dashboard/

You should be greeted by Bindaas Login page.

The logs can be found at <BINDAAS-BINARY-HOME>/log/bindaas.log.


## Log in to Bindaas Management Console

Log in using the default,
user name: admin
password: password



More details on configuring Bindaas can be found from the [admin guide](https://github.com/sharmalab/bindaas/wiki/Bindaas-Admin-Guide).

If you prefer to run Bindaas using its Docker container, please refer to [docker/README.md](DOCKER-README.md) for more details.



## Citing Bindaas
If you use Bindaas in your research, please cite the below paper:

* Kathiravelu, P., Saghar, Y.N., Aggarwal, T., and Sharma, A. **Data Services with Bindaas: RESTful Interfaces for Diverse Data Sources**. In *The IEEE International Conference on Big Data (BigData’19)*. pp. 457 - 462. Dec. 2019.
