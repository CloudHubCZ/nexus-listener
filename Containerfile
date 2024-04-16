# Use an official Ubuntu base image
FROM ubuntu:20.04

# Set environment variables to avoid interactive dialog during build
ENV DEBIAN_FRONTEND=noninteractive

# Install necessary packages
# and install Java OpenJDK 17
RUN apt-get update && apt-get install -y \
    wget \
    apt-transport-https \
    ca-certificates \
    gnupg \
    openjdk-17-jdk \
    software-properties-common \
    curl

# Verify Java installation
RUN java -version

# Install Docker
RUN wget -qO- https://get.docker.com/ | sh

# Add your user to the Docker group (optional, replace `root` with your user if it is not the root user)
RUN usermod -aG docker root

# Set up Docker daemon configuration for insecure registries
RUN mkdir -p /etc/docker && \
    echo '{ "insecure-registries":["108.141.226.214:8082"] }' > /etc/docker/daemon.json

# Expose Docker socket
VOLUME /var/run/docker.sock

# Add Skopeo's official PPA (Personal Package Archive) to get the latest version
RUN curl -fsSL https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/xUbuntu_20.04/Release.key | apt-key add - && \
    add-apt-repository "deb https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/xUbuntu_20.04/ /" && \
    apt-get update && \
    apt-get install -y skopeo

COPY target/*.jar app.jar

# Expose the port your Spring app runs on
EXPOSE 8080

# Run your Spring Boot application
CMD ["java", "-jar", "app.jar"]