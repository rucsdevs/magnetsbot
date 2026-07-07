# Deploying Star to AWS EC2

This guide provisions a free-tier EC2 instance and wires up the GitHub Actions
pipeline so every push to `main` redeploys the bot automatically.

## 1. Create the Discord application

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and create an application.
2. Under **Bot**, copy the token (this is `BOT_TOKEN`).
3. Enable the **Message Content Intent** under Privileged Gateway Intents (required for XP tracking).
4. Under **OAuth2 → URL Generator**, select the `bot` and `applications.commands` scopes, then invite the bot to your server with the generated URL. No special permissions are needed beyond sending messages.

## 2. Launch the EC2 instance

1. In the AWS console, launch an instance:
   - **AMI**: Ubuntu Server 24.04 LTS (arm64)
   - **Type**: `t4g.micro` (free tier eligible) — the bot idles at well under 256 MB
   - **Key pair**: create one, download the `.pem` file
   - **Security group**: allow inbound SSH (port 22) only. The bot makes outbound connections to Discord; nothing needs to reach it.
2. SSH in and install Docker:

```bash
ssh -i star.pem ubuntu@<EC2_HOST>
sudo apt-get update && sudo apt-get install -y docker.io
sudo usermod -aG docker ubuntu
# log out and back in for the group change to take effect
```

## 3. Configure GitHub repository secrets

In the repo, go to **Settings → Secrets and variables → Actions** and add:

| Secret | Value |
| --- | --- |
| `EC2_HOST` | Public DNS or IP of the instance |
| `EC2_SSH_KEY` | Contents of the `.pem` private key |
| `BOT_TOKEN` | Discord bot token |

## 4. Deploy

Push to `main` (or run the **Deploy** workflow manually from the Actions tab).
The pipeline builds the Docker image, ships it to the instance over SSH, and
restarts the container. XP data persists in the `star-data` Docker volume
across deployments.

Verify it worked:

```bash
ssh -i star.pem ubuntu@<EC2_HOST> docker logs -f star
```

You should see `Star is online in N guild(s)`.

## Running locally instead

```bash
# Option A: config file (create config.properties with BOT_TOKEN=...)
mvn package && java -jar target/star.jar

# Option B: Docker
docker build -t star .
docker run -d --name star -e BOT_TOKEN=your-token -v star-data:/data star
```

## Cost notes

- `t4g.micro` is covered by the AWS free tier for the first 12 months; after that it is roughly $6/month on-demand, or ~$2.50/month with a 1-year savings plan.
- There are no other billable resources: no load balancer, no RDS (SQLite lives on the instance), no NAT gateway.
