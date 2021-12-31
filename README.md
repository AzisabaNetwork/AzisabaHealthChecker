# AzisabaHealthChecker

```yml
debug: false
verbose: false
discordWebhook: null # default discord webhook url
servers:
  - name: test # user-friendly name (required)
    protocol: udp # tcp or udp (default: TCP)
    host: localhost:10000 # hostname:port (required)
    period: 1000 # default: 5000 milliseconds (optional)
  - name: test2
    protocol: tcp
    host: localhost:25599
    period: 5000
    threshold: 10 # default: 10
    discordWebhook: "" # set to empty string if you set webhook url above but don't want to send on this host
    webhookMessagePrefix: "@here\n" # prefix applies to every webhook messages
```
