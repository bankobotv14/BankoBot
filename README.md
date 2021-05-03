# Banko Bot

![CI](https://github.com/bankobotv14/BankoBot/workflows/.github/workflows/ci.yml/badge.svg)

This is a discord bot running on [@DerBanko](https://github.com/DerBanko)'s [Community Discord](https://banko.tv/discord). The Community is mainly focused around the programming of [Spigot](https://spigotmc.org) Plugins. There is a channel where people can ask for help when they don't no how to proceed. The bot provides some nice features for that.

## Requirements (for self hosting)
- [Mongo DB Server](https://docs.mongodb.com/manual/installation/)
- [JDoodle API key](https://www.jdoodle.com/compiler-api/) (Optional: needed for jdoodle command)
- [Google Custom Search Key](https://cse.google.com/cse/all) (Optional: needed for find command)
- [Twitch API application](https://dev.twitch.tv/console) (Optional: Required for stream status feature)
- HTTPS Reverse Proxy (Optional: needed for Twitch Integration)

# Env config

Example Config can be found [here](https://github.com/bankobotv14/BankoBot/blob/main/.env.example).

# Build
```bash
gradlew build
```

# Run
```bash
docker-compose up -d
```
