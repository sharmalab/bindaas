# Bindaas 3.0.1 Docker Container


## For the Bindaas User: Running the Docker Container

$ docker run --name bindaas-3 -p 8080:8080 -p 9099:9099 bindaas:3.0.1 


### To stop

Now you can stop the container using its assigned name:

$ docker stop bindaas-3


### To start the Bindaas instance again

Now you can stop the container using its assigned name:

$ docker start bindaas-3


If you do not remember the name of the Bindaas instance that was running, you may first find it to start it again.

$ docker ps -a


### Configure Data Source Providers.

If you are configuring data source providers to access the data sources from the host, your configurations will differ.

In Create Data Provider Action, make sure to use the below IP addresses instead of the default 127.0.0.1

docker.for.mac.host.internal (Docker for Mac)

host.docker.internal (for others)

More information - https://docs.docker.com/docker-for-mac/release-notes/#docker-community-edition-17120-ce-mac47-2018-01-12


## For the Bindaas Developer: Building the Docker Container

From the <BINDAAS-SOURCE-ROOT>/docker directory,

$ docker build -t bindaas:3.0.1 .

You will get the output "Successfully tagged bindaas:3.0.0" if everything went fine.


Confirm that by running

$ docker image ls

REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE

bindaas             3.0.1               a9a81d677bb2        5 minutes ago       630MB


Before committing, make sure Bindaas runs fine in the container using the command above listed under the "For the Bindaas User" section.


### Tag the image with the user name:
 
 $ docker tag bindaas:3.0.1 pradeeban/bindaas:3.0.1


 ### Log in and push the image to the Docker repository:

 $ docker login

 $ docker push pradeeban/bindaas:3.0.1
