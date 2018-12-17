# Bindaas 3.3.3 Docker Container


## For the Bindaas User: Running the Docker Container

$ sh docker-run.sh

The docker-run.sh script can be found inside the bin directory of the Bindaas binary or the 
distribution/build-extras/bin of the source code repository.

Edit the last line of the docker-run.sh to point to the correct locations of the bin, bundles, and log folders of Bindaas.

### Configure Data Source Providers.

If you are configuring data source providers to access the data sources from the host, your configurations will differ.

In Create Data Provider Action, make sure to use the below IP addresses instead of the default 127.0.0.1

docker.for.mac.host.internal (Docker for Mac)

host.docker.internal (for others)

More information - https://docs.docker.com/docker-for-mac/release-notes/#docker-community-edition-17120-ce-mac47-2018-01-12


## For the Bindaas Developer: Building the Docker Container

From the <BINDAAS-DIST-ROOT>/bin directory,

$ docker build -t bindaas:3.3.3 .

You will get the output "Successfully tagged bindaas:3.3.3" if everything went fine.


Confirm that by running

$ docker image ls

REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE

bindaas             3.3.3              a9a81d677bb2        5 minutes ago        59MB



### Tag the image with the user name:
 
 $ docker tag bindaas:3.3.3 pradeeban/bindaas:3.3.3


 ### Log in and push the image to the Docker repository:

Before committing, make sure Bindaas runs fine in the container using the command above listed under the "For the Bindaas User" section.

 $ docker login

 $ **docker push pradeeban/bindaas:3.3.3**
