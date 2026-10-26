# Move My Arubaito off Railway onto a small Hetzner VPS

This API is a single Spring Boot process plus MySQL. The website itself stays on Vercel. The goal is to run the API and the database on one Ubuntu server (about 4 GB of RAM) using [Coolify](https://coolify.io), which builds this repo from the `Dockerfile`.

Do the work in this order: bring the new server up, copy the database, test, then point the site at it. Leave Railway running until the new server has been used for real. That is what makes rollback possible.

Prices below were checked against Hetzner's site in October 2026. Confirm them before you order. They change, and the cheap 4 GB plans sell out for days at a time.

## What this costs

| | Rough price |
| --- | --- |
| Railway today | about $60 CAD / month |
| Hetzner CX23 (2 vCPU, 4 GB RAM, 40 GB disk) | about €5.99 / month before tax, hourly with a monthly cap. IPv4 is included in that figure on the cost-optimized page. |
| Optional Hetzner server backups | about 20% extra |
| Coolify | free (it runs on the same server) |
| Domain you already have | no change |

€6 is on the order of $9–10 CAD, so the hosting bill should drop by roughly $50 CAD a month. VAT and a backup add-on are the only extras that matter.

Still billed separately, because they are not Railway:

- Vercel, for the Next.js site
- AWS KMS, for the encryption key. Job titles, hourly rates, shift pay, and location are encrypted with that key. The database dump is useless for those fields without the same key.
- Google Gemini, for schedule screenshots
- Gmail, for password-reset mail

A 4 GB server is enough if you cap memory the way this guide says. Do not add a second app on it.

If CX23 is sold out, CAX11 (ARM, 4 GB) is the cheaper cousin and this image runs on it too. CPX22 (about €8 / month after the April 2026 price change) is the easy x86 fallback. Skip anything smaller than 4 GB. MySQL and a Java API together do not fit comfortably in 2 GB once Coolify is on the box.

## 1. Create the server

1. In [Hetzner Cloud](https://console.hetzner.com), create a project and add an SSH key from your computer (`ssh-keygen -t ed25519` if you do not have one).
2. Create a server:
   - Location: Falkenstein or Nuremberg (Helsinki is fine too). Pick whichever shows the 4 GB plan in stock.
   - Image: Ubuntu 24.04
   - Type: CX23, or CAX11 / CPX22 if CX23 is unavailable
   - Networking: public IPv4 and IPv6. You need the IPv4 address for DNS.
   - SSH key: the one you just added. Do not enable password login.
   - Name: `myarubaito`
3. Optional but worth it: enable Hetzner Backups on the server (the 20% add-on). That snapshots the whole disk. It is not a substitute for a MySQL dump, but it saves you if the disk dies.
4. Note the public IPv4 address.

SSH in:

```bash
ssh root@YOUR_SERVER_IP
```

Then, still as root:

```bash
apt-get update && apt-get upgrade -y
```

Reboot if the upgrade says the kernel changed, then SSH back in.

## 2. Install Coolify

Coolify's install command changes occasionally. Read [the install page](https://coolify.io/docs/get-started/installation) first, then run the command they show. As of this writing it is:

```bash
curl -fsSL https://cdn.coollabs.io/coolify/install.sh | bash
```

The script installs Docker and Coolify. It takes a few minutes. At the end it prints a URL on port 8000, like `http://YOUR_SERVER_IP:8000`.

Open that URL, create the admin account, and store the password in a password manager. This admin account is the keys to the server.

In the Hetzner firewall (or the server firewall, if you enable one), allow:

- 22 (SSH) from your own IP only, if you are comfortable locking it down
- 80 and 443 from anywhere (the site)
- 8000 from your own IP only (the Coolify dashboard)

Do not leave MySQL's port (3306) open to the internet. Coolify can reach the database on the internal Docker network.

Point a hostname at the server for the dashboard if you want, for example `coolify.myarubaito.com`, an A record to the IPv4. Coolify will ask for this and issue a certificate. You can also keep using the IP and port.

## 3. Create MySQL in Coolify

Create the database before the app, and import the Railway dump into it before the app starts writing.

1. In Coolify, create a project (for example `myarubaito`) and an environment (`production`).
2. Add a resource of type **Database → MySQL**. Choose MySQL 8.
3. Set:
   - database name: `myarubaito` (or match the Railway database name; the dump contains the name)
   - user and password: generate them and save them
   - root password: generate a different one and save it
4. Do **not** turn on a public port. The API will use Coolify's internal hostname.
5. In the database settings, set a memory limit of **768 MB** if the screen offers one.
6. If there is a custom configuration or command field, add the same cheap settings as `docker-compose.yml`:

   ```
   innodb_buffer_pool_size=256M
   performance_schema=OFF
   max_connections=40
   character-set-server=utf8mb4
   collation-server=utf8mb4_unicode_ci
   ```

   `performance_schema=OFF` is the one that saves the most memory. Skip a line if Coolify's form rejects it, then start MySQL and move on.

7. Deploy the database and wait until it is running.
8. Copy the **internal** connection details Coolify shows (host, port, database, user, password). The host looks like a container name, not a public IP. You will use it in the app's environment.

## 4. Copy the data out of Railway

Do this while Railway is still serving traffic. You will take one more dump immediately before the DNS switch, because people may add shifts in between.

Install a MySQL client on your laptop if you need one (`brew install mysql-client` on a Mac, or use a one-off Docker container). From Railway's MySQL service, open the Connect tab and enable public networking if it is not already on. Copy the host, port, user, password, and database name.

```bash
mysqldump \
  --single-transaction \
  --routines \
  --triggers \
  --set-gtid-purged=OFF \
  --no-tablespaces \
  -h RAILWAY_HOST -P RAILWAY_PORT -u RAILWAY_USER -p \
  RAILWAY_DATABASE > myarubaito-$(date +%F).sql
```

Type the password when asked. Do not put it in the command, and do not commit the `.sql` file.

If the client complains about `COLUMN_STATISTICS`, add `--column-statistics=0`.

Check the file before you trust it:

```bash
ls -lh myarubaito-*.sql
grep -c "INSERT INTO" myarubaito-*.sql
```

The file should be more than a few kilobytes and the INSERT count should be greater than zero if you have real users. Copy it to a second place (a USB disk or a private cloud folder). This file is your database.

### Import it into Coolify

SSH to the Hetzner server. Coolify's MySQL container name is on the database page. From the server:

```bash
docker ps --format '{{.Names}}' | grep -i mysql
docker exec -i THAT_CONTAINER_NAME \
  mysql -u myarubaito -p myarubaito < /path/to/myarubaito-YYYY-MM-DD.sql
```

You will be prompted for the Coolify database password. To get the file onto the server:

```bash
scp myarubaito-YYYY-MM-DD.sql root@YOUR_SERVER_IP:/root/
```

Delete `/root/myarubaito-*.sql` from the server after a successful import. Do not leave a copy of production data sitting in `/root`.

If the dump says `CREATE DATABASE` / `USE` and those names differ from the Coolify database, either edit those two lines to the Coolify name or import with the Railway name and point the app at that name. Look at the top of the `.sql` file before importing.

Confirm the tables arrived:

```bash
docker exec -it THAT_CONTAINER_NAME mysql -u myarubaito -p myarubaito -e "SHOW TABLES;"
```

You want the worker, job, shift, and settings tables. Do not start the app with `DDL_AUTO=create` or `create-drop` after this. Those settings drop data.

## 5. Deploy the API from this repo

1. In the Coolify project, add a resource of type **Public** or **Private Git repository**. Use the GitHub repo for this project.
2. Build pack: **Dockerfile**. Branch: the branch that contains this file (then `main` after you merge).
3. Port: `8080`. Coolify's proxy talks to that port. Leave `SERVER_PORT` unset unless you change the port in both places.
4. Set the app memory limit to **1024 MB**. This matters. The JVM is set to use about 60% of at most 1 GB (around 600 MB of heap). If you do not set a limit, `MaxRAM=1024m` in `JAVA_OPTS` still stops the heap from eating the whole machine. MySQL is capped separately at 768 MB. The rest of the 4 GB is for Ubuntu and Coolify.
5. Domain: `api.myarubaito.com` (or whatever the site already calls). Add the DNS A record to the server IPv4 when Coolify tells you to. Start with Coolify's temporary domain if you want to test before touching DNS.
6. Paste the environment variables from the next section, then deploy.

The first build downloads a JDK image and Maven. Later builds are faster. The running container is a JRE only, and the process is not root.

Watch the logs for `Started MyArubaitoDashApplication` and for `HikariPool-1 - Start completed` or the pool name `myarubaito`. If the log says the `mysql://` URL was rewritten, that is expected when you paste Coolify's internal URL.

### Environment variables

Copy the values from the Railway service. Do not invent a new `JWT_SECRET`. A new secret logs every user out.

| Variable | What to put |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Coolify internal URL. Either `jdbc:mysql://HOST:3306/myarubaito?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8` or the `mysql://user:pass@HOST:3306/myarubaito` URL Coolify shows. The app converts the `mysql://` form. |
| `SPRING_DATASOURCE_USERNAME` | MySQL user from step 3. Optional if the URL already contains it. |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password from step 3. Optional if the URL already contains it. |
| `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` | Leave unset on Coolify. These are the Railway names and still work if you would rather copy them unchanged. `DATABASE_URL` wins over `MYSQL_URL` and over the `MYSQLHOST` pieces. |
| `DDL_AUTO` | The value from Railway. If Railway has no such variable, use `update`. Never `create` or `create-drop` on a database you imported. |
| `APP_PROFILE` | Leave unset for the first deploy. Unset means `local`, which is what the app does on Railway today. Set `prod` only if login cookies need `SameSite=None`. The `prod` profile also hardcodes the cookie domain `.myarubaito.com` in `SecurityConfig`. |
| `SESSION_COOKIE_SECURE` | `true` on the public site. `false` only for plain http on your laptop. |
| `COOKIE_DOMAIN_NAME` | Copy from Railway. Often `.myarubaito.com` (leading dot) so the site and the API can share a cookie. |
| `ALLOWED_ORIGINS` | Copy from Railway. Comma-separated, no spaces required. Example: `https://myarubaito.com,https://www.myarubaito.com` |
| `SERVER_FORWARD_HEADERS_STRATEGY` | `framework` on Coolify. Coolify's proxy terminates HTTPS. Leave this unset on Railway so the old behavior stays. |
| `AWS_KMS_CMK_ARN` | Copy from Railway. Same key as before. |
| `AWS_ACCESS_KEY_ID` | Copy from Railway. |
| `AWS_SECRET_ACCESS_KEY` | Copy from Railway. |
| `AWS_REGION` | `us-east-2`, unless the key lives somewhere else. That region was previously hardcoded. |
| `SPRING_MAIL_USERNAME` | Copy from Railway (Gmail address). |
| `SPRING_MAIL_PASSWORD` | Copy from Railway (Gmail app password). |
| `SPRING_MAIL_HOST` | Leave unset. Default is `smtp.gmail.com`. |
| `SPRING_MAIL_PORT` | Leave unset. Default is `587`. |
| `GEMINI_API_KEY` | Copy from Railway. |
| `JWT_SECRET` | Copy exactly. At least 32 characters. |
| `JWT_EXPIRATION` | Copy exactly. It is a number of seconds. |
| `RESET_PASSWORD_URL` | Copy from Railway. This is the link in the reset email, so it must be the real site URL. |
| `HIKARI_MAXIMUM_POOL_SIZE` | `5` (the new default). Raise it only if you see timeout errors waiting for a connection. |
| `HIKARI_MINIMUM_IDLE` | `1` (the new default). |
| `JAVA_OPTS` | Leave as the image default unless you change the 1024 MB limit. If you give the app 1536 MB and want a bigger heap, set `JAVA_OPTS` to the default string from the `Dockerfile` and raise `-XX:MaxRAM`. Setting `JAVA_OPTS` replaces the whole string. |
| `SERVER_PORT` | Leave unset. The app listens on 8080. |

`MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`, `MYSQL_URL`, and `MYSQLURL` are also read, for a Railway MySQL plugin reference. You do not need them on Coolify if `SPRING_DATASOURCE_URL` is set.

Nothing in this list belongs in git. Coolify's environment screen stores them on the server.

## 6. Turn on backups

A Hetzner disk snapshot is not a MySQL backup. Take both.

1. Open the MySQL resource in Coolify → **Backups** → add a schedule. Daily is enough, for example `0 4 * * *` (04:00 on the server).
2. Keep at least 7 copies.
3. Add S3 storage. [Hetzner Object Storage](https://www.hetzner.com/storage/object-storage/) is the cheap option and Coolify speaks the S3 API. Create a bucket, then in Coolify add an S3 destination with that endpoint, key, and secret. Attach it to the schedule so a dead VPS does not take the dumps with it.
4. Run one backup by hand. Download it, and restore it into a throwaway database (a second empty MySQL, or your laptop) once. A backup you have never restored is a hope, not a backup.
5. The scheduled job only runs while the database container is running. Coolify uses `mysqldump`, and it needs `MYSQL_ROOT_PASSWORD` and `MYSQL_DATABASE` on that container. Those are set if you created the database in the Coolify UI.

Also keep the laptop dump from step 4 until the new server has survived a week.

## 7. Test before you switch

On the Coolify temporary URL, or on `api.myarubaito.com` if you already pointed only that name at the new server and the Vercel site still calls Railway:

```bash
curl -s -o /dev/null -w "%{http_code}\n" https://YOUR_API_HOST/api/validate-session?userId=0
```

`401` or `400` means the app answered. `000` or a timeout means it did not. An empty connection refused means the proxy or the container is down.

Then, from the real site pointed at the new API (you can do this with a hosts-file override, or by changing the Vercel API URL in a preview):

- Log in. This checks `JWT_SECRET`.
- Open jobs and shifts. Titles and pay are decrypted with AWS KMS. If this fails, the key ARN, the region, or the AWS keys are wrong. Fix that before switching. Those rows are ciphertext without KMS.
- Add a shift on a test account and refresh. It should survive a restart of the app container (the data is in MySQL, not in the container).
- Request a password reset to yourself. This checks Gmail.
- If you use screenshot import, try one image. This checks Gemini.

Restart the app once in Coolify and log in again. Then reboot the VPS and confirm both containers come back. Coolify sets Docker restart policies; check that MySQL and the app are running after the reboot.

## 8. Switch, then turn Railway off

1. Lower DNS TTL to 300 seconds a day before, if you control the API hostname.
2. Take a fresh `mysqldump` from Railway (step 4) and import it again, replacing the Coolify data. Do this at a quiet hour. Anything saved on Railway after this dump will not be on the new server.
3. Point the site at the new API:
   - If the frontend calls a `railway.app` URL, change that environment variable in Vercel to `https://api.your-domain` and redeploy the site.
   - If `api.myarubaito.com` is a CNAME to Railway, change it to an A record for the Hetzner IPv4.
   - `ALLOWED_ORIGINS` on the API must include the exact origin the browser uses (`https://myarubaito.com`, with no path).
4. Test login, a shift, and one write from your phone, not only your laptop.
5. Leave Railway up for a few days but stop deploying to it. If you need to roll back, see below. New shifts created on Hetzner during those days are not on Railway.
6. When you are satisfied, download one more dump from the Hetzner MySQL, then delete the Railway MySQL and the Railway app. Deleting the Railway project is what stops the bill. Pausing a volume can still cost money. Check the Railway billing page the next day and confirm usage has stopped.

## Rollback

Railway is the rollback, for as long as you have not deleted it and have not taken writes you cannot afford to lose.

- If the new API fails before you switch DNS or the Vercel URL: do nothing to Railway. Fix Coolify, or leave it and stay on Railway.
- If you already switched and the new API is broken: point the Vercel API URL and the DNS record back to Railway. Users see the old data. Shifts entered on Hetzner after the import are not on Railway. If those writes matter, `mysqldump` the Hetzner database before pointing DNS back, and accept that merging the two copies by hand is painful. This is why the switch should happen when nobody is adding shifts.
- Do not delete the Railway database until the new one has been restored from a backup at least once and you have used the site for real.

## Shut the app down instead of moving it

If you retire the product rather than move it, the cheap version is: keep the dump, delete the running services, and do not keep a VPS you are not using.

1. `mysqldump` Railway exactly as in step 4. Run the `ls` and `INSERT INTO` checks.
2. Store the file in two places you control. Remember it contains people, emails, and encrypted pay data.
3. Write down the AWS KMS key id and the region, and keep the AWS account. Job titles, wages, shift pay, and location cannot be read later without that key. Do not schedule the key for deletion unless you are sure you will never want the data. KMS deletion has a waiting period, and after that the ciphertext in the dump is permanent garbage.
4. Stop the Railway app service, then the Railway MySQL service.
5. Delete the Railway project so the usage charges stop. Confirm on the billing page the next day.
6. On Vercel, either leave the frontend (it will error with no API) or remove the project if you do not want the site up.
7. Gemini and Gmail stop costing money when nothing calls them. Rotate or delete the Gemini key and the Gmail app password so a leaked Railway environment cannot use them.
8. There is no Hetzner bill if you never create the server. If you already did, delete the server in the Hetzner console. Deleting is what stops the hourly charge. A powered-off server can still be billed.

Keep the dump even if you delete everything else. It is the only copy of the shifts.

## Local rehearsal

You can practice the same layout on your computer before touching Hetzner. From this repo:

```bash
docker compose up --build
```

The API listens on `http://localhost:8080` and MySQL on port 3306. The passwords in `docker-compose.yml` are fake and local. Details are in the README.
