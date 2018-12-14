# Bindaas 3.3.2 Docker Container


## For the Bindaas User: Running the Docker Container

$ sh run.sh


### Configure Data Source Providers.

If you are configuring data source providers to access the data sources from the host, your configurations will differ.

In Create Data Provider Action, make sure to use the below IP addresses instead of the default 127.0.0.1

docker.for.mac.host.internal (Docker for Mac)

host.docker.internal (for others)

More information - https://docs.docker.com/docker-for-mac/release-notes/#docker-community-edition-17120-ce-mac47-2018-01-12


## For the Bindaas Developer: Building the Docker Container

From the <BINDAAS-SOURCE-ROOT>/docker directory,

$ docker build -t bindaas:3.3.2 .

You will get the output "Successfully tagged bindaas:3.3.2" if everything went fine.


Confirm that by running

$ docker image ls

REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE

bindaas             3.3.2              a9a81d677bb2        5 minutes ago       359MB



### Tag the image with the user name:
 
 $ docker tag bindaas:3.3.2 pradeeban/bindaas:3.3.2


 ### Log in and push the image to the Docker repository:

Before committing, make sure Bindaas runs fine in the container using the command above listed under the "For the Bindaas User" section.

 $ docker login

 $ docker push pradeeban/bindaas:3.3.2
