# PermGuard 🔒

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.5+-brightgreen)](https://papermc.io/software/paper)
[![Java Version](https://img.shields.io/badge/java-16+-orange)](https://adoptium.net/installation/linux/)
[![GitHub Release](https://img.shields.io/github/v/release/alex2276564/PermGuard?color=blue)](https://github.com/alex2276564/PermGuard/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![ISO/IEC 27001](https://img.shields.io/badge/ISO/IEC%2027001-Compliant-brightgreen)](https://www.iso.org/isoiec-27001.html)
[![CIS Controls](https://img.shields.io/badge/CIS%20Controls-Compliant-brightgreen)](https://www.cisecurity.org/)
[![Least Privilege Principle](https://img.shields.io/badge/Least%20Privilege%20Principle-Implemented-brightgreen)](https://en.wikipedia.org/wiki/Principle_of_least_privilege)
[![Audit Logging](https://img.shields.io/badge/Audit%20Logging-Enabled-yellow)](https://en.wikipedia.org/wiki/Audit_trail_(information_security))

**PermGuard** is a Minecraft plugin designed to enhance server security by temporarily revoking admin permissions upon joining the server. This helps to prevent unauthorized access and potential security breaches. When an admin with elevated permissions joins the server, their permissions are removed, and they are kicked from the server. The permissions can only be restored after the admin rejoins without the elevated permissions and manually re-grants them via the console using commands like `lp user playernick permission set *`.

## ✨ Features

* **Automatic Permission Revoke:** Revokes specified permissions when an admin joins.
* **Kick on Elevated Permissions:** Kicks the admin and logs the event if elevated permissions are detected.
* **Manual Permission Restoration:** Admins must rejoin without elevated permissions and restore them manually via console commands.
* **Customizable Kick Messages:** You can personalize the message shown to admins when they are kicked.
* **Logging:** Logs all permission revocation events to both the console and a file for auditing.
* **Reload Command:** Allows reloading the configuration without restarting the server.
* **Lightweight and Efficient:** Designed to have minimal impact on server performance.
* **Shutdown Protection:** If the plugin is disabled (e.g., through Plugman), the server will automatically shut down to ensure no security gaps are left open.
* **Auto-Update Check:** On server start, the plugin checks for updates. If a new version is available, a notification is displayed in the console.

## 🛡️ Security Benefits

### How PermGuard Protects Against Advanced Attacks:

1. **Brute Force Attacks:** By removing permissions upon login, it prevents unauthorized access even if someone gains temporary access to an admin account.
2. **Account Compromise:** If an admin's account is compromised, the attacker won't be able to abuse their permissions as they will be revoked upon joining.
3. **Session Hijacking:** Mitigates the risk of session hijacking by ensuring that permissions are not persistently available during a hijacked session.
4. **Social Engineering:** Reduces the risk of admins being tricked into giving away their permissions.
5. **Unknown Vulnerabilities:** Helps mitigate the risks associated with unknown vulnerabilities by limiting the potential damage an attacker can do, even if they find a way to bypass other security measures.
6. **Port Exploitation / BungeeCord Hacks:** By revoking admin permissions on entry, PermGuard minimizes the potential for attackers to exploit server ports or use BungeeCord-related hacks to gain elevated privileges.
7. **AuthMe Bypass:** Even if an attacker finds a way to bypass AuthMe authentication, they won't gain immediate access to admin permissions as PermGuard will revoke them upon entry.
8. **Zero-Day Exploits:** Provides a safety net against zero-day exploits by ensuring that even if a vulnerability is exploited, the attacker will not have sustained access to admin permissions.
9. **Shutdown Protection:** Ensures that if the plugin is disabled , the server shuts down to prevent any security gaps from being exploited.

### Compliance with Security Standards:

- **Least Privilege Principle (ISO/IEC 15408):** Ensures that users have only the minimum permissions necessary to perform their tasks, reducing the risk of privilege abuse.
- **Audit Logging (ISO/IEC 27001):** Provides detailed logs of all permission-related activities, facilitating compliance audits and forensic analysis.
- **ISO/IEC 27001 Compliance:** PermGuard helps servers adhere to information security management best practices by enforcing strict permission controls and audit logging.
- **CIS Controls:** Aligns with the Center for Internet Security (CIS) Controls for effective cyber defense by implementing strong access control measures.
- **Secure Access Control:** By requiring manual permission restoration via the console, PermGuard ensures that only authorized personnel can grant elevated privileges.

## 📥 Installation

1. **Download:** Download the latest version of PermGuard from the [Releases](https://github.com/alex2276564/PermGuard/releases) page.
2. **Install:** Place the `.jar` file into your server's `plugins` folder.
3. **Restart:** Restart your server to load the plugin.

## 🛠️ Configuration

Edit the `config.yml` file in the plugin's folder to customize settings:

```yaml
# PermGuard Configuration

# List of restricted permissions
restrictedPermissions:
  # Wildcard permission (Should always be first in the list if used)
  - permission: "*"
    # Command to execute when detected (use %player% and %permission% as placeholders)
    cmd: "lp user %player% permission unset %permission%"
    # Whether to log this violation
    log: true
    # Message to show player when kicked (use %permission% as placeholder)
    kickMessage: "Your %permission% permissions have been revoked. Please rejoin and restore them via console."

  # Admin group permission
  - permission: "group.admin"
    cmd: "lp user %player% permission unset %permission%"
    log: true
    kickMessage: "Your admin permissions have been revoked. Please rejoin and restore them via console."

  # Example if you use op instead of * in LuckPerms
  #- permission: "*"
  #  cmd: "deop %player%"
  #  log: true
  #  kickMessage: "Your op permissions have been revoked. Please rejoin and restore them via console."
```

## 📜 Commands

- `/permguard reload` - Reloads the plugin configuration (requires `permguard.reload` permission)

## 🛠️ Compatibility

- **Minecraft Versions:** 1.16.5 to the latest release
- **Server Software:** [Paper](https://papermc.io/) (1.16.5 and newer)

## 📦 Other Plugins

Also check out my other plugins for protecting your Minecraft server:

- [**LeverLock**](https://github.com/alex2276564/LeverLock)  
  *LeverLock* - a plugin to prevent rapid lever interactions, which can cause lag or be exploited for unintended game mechanics. Works in conjunction with **AntiRedstoneClock-Remastered**, providing comprehensive protection from redstone-based lag and exploits.

- [**NoMoreTNTChainCrash**](https://github.com/alex2276564/NoMoreTNTChainCrash)  
  *NoMoreTNTChainCrash* is a Minecraft plugin designed to prevent server crashes and lag caused by excessive TNT explosions. It achieves this by removing TNT before automated chain explosions can occur, while still allowing players to manually detonate TNT as desired.

## 🆘 Support

If you encounter any issues or have suggestions for improving the plugin, please create an [issue](https://github.com/alex2276564/PermGuard/issues) in this repository.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

[Alex] - [https://github.com/alex2276564]

We appreciate your contribution to the project! If you like this plugin, please give it a star on GitHub.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/alex2276564/PermGuard/issues).

### How to Contribute

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a Pull Request.

---

Thank you for using **PermGuard**! We hope it enhances the security of your Minecraft server significantly, making unauthorized access and privilege abuse a thing of the past. 🎮🔒
