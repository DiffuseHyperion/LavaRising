### this was my first plugin and github repo, excuse me for cringe description haha

# LavaRising
mostly did this cuz i could, and all other lava rising plugins at the time was broken or paid

nothing much of value here, this is absolute spagetti code, but have fun on it

instructions on the spigot download page, but ill copy paste them here:

## Instructions:

- **Use this plugin in a separate server! Ideally, it should be the only plugin in the server.**

- **DO NOT reload your server when using this plugin! Use /restart instead.**

- This plugin will restart your server when the game ends. 
  - For linux users, put a ./start.sh file in the server directory.
  - For windows users, you can edit your batch file to be something like this:

```
@Echo off
:start:
cls
<your original server launch script>
goto start
```

- if you want to, you can put a "pause" right before "goto start", like this: 

```
<your original server launch script>
pause
goto start
```

- so that the server waits for your input on the cmd prompt before restarting

- i guess you could also edit the restarting file in spigot.yml, but never tried it before myself

# Support
Contact me via DMS on discord at DiffuseHyperion#0014 if the plugin dies lol
