# Banko Bot

![CI](https://github.com/bankobotv14/BankoBot/workflows/.github/workflows/ci.yml/badge.svg)

# Requirements
- [Mongo DB server](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/)
- [JDoodle API key](https://www.jdoodle.com/compiler-api/) (Optional: needed for eval command)
- [Google Custom Search Key](https://cse.google.com/cse/all) (Optional: needed for find command)
- [Twitch API application](https://dev.twitch.tv/console) (Optional: Required for stream status feature)  
- HTTPS Reverse Proxy (Optional: needed for Twitch)

# Env config: https://github.com/bankobotv14/BankoBot/tree/main/src/main/kotlin/de/nycode/bankobot/config

# Build
```bash
./gradlew build
```

# Run
```bash
docker-compose up -d
```
