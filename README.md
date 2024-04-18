# Podman commands
podman build --platform linux/amd64 -t davidmachacek/nexus-listener:20240416.1 -f Containerfile && podman push avidmachacek/nexus-listener:20240416.1

# Vulnerable images for scan
108.141.226.214:8082/vulnerable-app:latest

podman login --tls-verify=false --verbose --username "admin" --password "admin" 98.64.251.137:8082
podman push 98.64.251.137:8082/vulnerable-app:latest --tls-verify=false