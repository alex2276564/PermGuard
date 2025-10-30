# PermGuard 🔒

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.5+-brightgreen)](https://papermc.io/software/paper)
[![Java Version](https://img.shields.io/badge/java-17+-orange)](https://adoptium.net/installation/linux/)
[![GitHub Release](https://img.shields.io/github/v/release/alex2276564/PermGuard?color=blue)](https://github.com/alex2276564/PermGuard/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![ISO/IEC 27001](https://img.shields.io/badge/ISO/IEC%2027001-Compliant-brightgreen)](https://www.iso.org/isoiec-27001.html)
[![Zero Trust Architecture](https://img.shields.io/badge/Zero%20Trust-Architecture-blue)](https://www.nist.gov/publications/zero-trust-architecture)
[![CIS Controls](https://img.shields.io/badge/CIS%20Controls-Compliant-brightgreen)](https://www.cisecurity.org/)
[![Least Privilege Principle](https://img.shields.io/badge/Least%20Privilege%20Principle-Implemented-brightgreen)](https://en.wikipedia.org/wiki/Principle_of_least_privilege)
[![Audit Logging](https://img.shields.io/badge/Audit%20Logging-Enabled-yellow)](https://en.wikipedia.org/wiki/Information_security)
[![Telegram Notifications](https://img.shields.io/badge/Telegram-Notifications-blue)](https://core.telegram.org/bots/api)
[![Text Formatting](https://img.shields.io/badge/Text%20Formatting-🌈%20MiniMessage-ff69b4)](https://docs.advntr.dev/minimessage/)

**PermGuard** is a Minecraft plugin designed to enhance server security by temporarily revoking admin permissions upon
joining the server and sending security alerts to Telegram. Unlike traditional admin password plugins, which can often
be bypassed through various exploits, PermGuard implements a fundamentally more secure approach by completely removing
elevated permissions on join. This helps to prevent unauthorized access and potential security breaches, even if other
security measures are compromised. When an admin with elevated permissions joins the server, their permissions are
removed, and they are kicked from the server. The permissions can only be restored after the admin rejoins without the
elevated permissions and manually re-grants them via the console using commands like
`lp user playernick permission set *`.

## ✨ Features

* **Automatic Permission Revoke:** Revokes specified permissions when an admin joins.
* **Kick on Elevated Permissions:** Kicks the admin and logs the event if elevated permissions are detected.
* **Telegram Alerts:** Sends detailed security notifications to Telegram, including information about the player, their
  IP address, and country.
* **Manual Permission Restoration:** Admins must rejoin without elevated permissions and restore them manually via
  console commands.
* **Customizable Kick Messages:** You can personalize the message shown to admins when they are kicked.
* **Logging:** Logs all permission revocation events to both the console and a file for auditing.
* **Reload Command:** Allows reloading the configuration without restarting the server.
* **Lightweight and Efficient:** Designed to have minimal impact on server performance.
* **Shutdown Protection:** If the plugin is disabled (e.g., through Plugman), the server will automatically shut down to
  ensure no security gaps are left open.
* **Auto-Update Check:** On server start, the plugin checks for updates. If a new version is available, a notification
  is displayed in the console.
* **Modern Text Rendering:** Uses Adventure MiniMessage for sleek formatting on supported servers (Paper 1.18+), with
  automatic fallback on older versions.

## 🛡️ Security Benefits

### How PermGuard Protects Against Advanced Attacks:

1. **Brute Force Attacks:** By removing permissions upon login, it prevents unauthorized access even if someone gains
   temporary access to an admin account.
2. **Account Compromise:** If an admin's account is compromised, the attacker won't be able to abuse their permissions
   as they will be revoked upon joining.
3. **Session Hijacking:** Mitigates the risk of session hijacking by ensuring that permissions are not persistently
   available during a hijacked session.
4. **Social Engineering:** Reduces the risk of admins being tricked into giving away their permissions.
5. **Unknown Vulnerabilities:** Helps mitigate the risks associated with unknown vulnerabilities by limiting the
   potential damage an attacker can do, even if they find a way to bypass other security measures.
6. **Port Exploitation / BungeeCord Hacks:** By revoking admin permissions on entry, PermGuard minimizes the potential
   for attackers to exploit server ports or use BungeeCord-related hacks to gain elevated privileges.
7. **AuthMe Bypass:** Even if an attacker finds a way to bypass AuthMe authentication, they won't gain immediate access
   to admin permissions as PermGuard will revoke them upon entry.
8. **Zero-Day Exploits:** Provides a safety net against zero-day exploits by ensuring that even if a vulnerability is
   exploited, the attacker will not have sustained access to admin permissions.
9. **Telegram Alerts:** If a suspicious activity is detected, detailed notifications are sent to Telegram, including the
   player's IP and country, so you can take immediate action.
10. **Shutdown Protection:** Ensures that if the plugin is disabled , the server shuts down to prevent any security gaps
    from being exploited.

### Compliance with Security Standards:

**Note:** The following compliance features are implemented within the PermGuard plugin itself. Your server's overall
security compliance depends on your complete infrastructure setup, proper configuration of all components, and following
security best practices across your entire system.

- **Least Privilege Principle (ISO/IEC 15408):** Ensures that users have only the minimum permissions necessary to
  perform their tasks, reducing the risk of privilege abuse.
- **Audit Logging (ISO/IEC 27001):** Provides detailed logs of all permission-related activities, facilitating
  compliance audits and forensic analysis.
- **ISO/IEC 27001 Compliance:** PermGuard helps servers adhere to information security management best practices by
  enforcing strict permission controls and audit logging.
- **CIS Controls:** Aligns with the Center for Internet Security (CIS) Controls for effective cyber defense by
  implementing strong access control measures.
- **Secure Access Control:** By requiring manual permission restoration via the console, PermGuard ensures that only
  authorized personnel can grant elevated privileges.
- **Zero Trust Architecture (NIST SP 800-207):**  Applies a deny-by-default philosophy to administrative privileges.
  Elevated access is never implicitly trusted and must be explicitly restored via the console after join.

## 🛡️ Zero Trust Security Model

**PermGuard** implements a **Zero Trust Architecture** for Minecraft server security - instead of relying on
authentication checks, passwords, or trust assumptions, it **unconditionally revokes elevated permissions** upon every
login.

### Why Zero Trust?

Traditional security plugins rely on **verification mechanisms** (passwords, 2FA, IP checks) which can be:

- 🔓 **Bypassed** through exploits (AuthMe vulnerabilities, session hijacking)
- 🔓 **Compromised** via social engineering or credential theft
- 🔓 **Circumvented** by unknown zero-day vulnerabilities

**PermGuard's approach is fundamentally different:**

> **"Never trust, always revoke"** - Permissions are removed *before* any verification, making the security model *
*attack-method agnostic**. Even if an attacker bypasses all authentication layers, they gain **no elevated privileges**
> because those privileges simply don't exist until manually restored via console.

This makes PermGuard effective against:

- ✅ **Known attack vectors** (brute force, AuthMe bypass, BungeeCord exploits)
- ✅ **Unknown future exploits** (zero-day vulnerabilities)
- ✅ **Advanced persistent threats** (compromised accounts, session hijacking)
- ✅ **Insider threats** (unauthorized access by trusted users)

### TL;DR

`PermGuard implements the principle of “zero trust” — instead of relying on checks and passwords, it simply revokes permissions upon any login. This makes it effective even against unknown types of attacks, as the basic security principle works regardless of the attack method.`

## 📥 Installation

1. **Download:** Download the latest version of PermGuard from
   the [Releases](https://github.com/alex2276564/PermGuard/releases) page.
2. **Install:** Place the `.jar` file into your server's `plugins` folder.
3. **Restart:** Restart your server to load the plugin.

## 📜 Commands

PermGuard supports both the full command `/permguard` and the shorter alias `/pg` for all commands (requires
`permguard.command` permission).

- `/pg help` - Show help information (requires `permguard.command`)
- `/pg reload` - Reloads the plugin configuration (requires `permguard.reload` permission)

## 🛠️ Compatibility

- **Minecraft Versions:** 1.16.5 to the latest release
- **Server Software:**
    - ✅ [Paper](https://papermc.io/) (1.16.5 and newer) - **Fully Supported**
    - ✅ [Folia](https://papermc.io/software/folia) - **Fully Supported** with optimized region-aware scheduling
    - ❌ Spigot - Not supported
- **Java Version:** Java 17 or higher

## 📝 Note

**Security Implementation Design:** PermGuard uses `PlayerJoinEvent` instead of `PlayerLoginEvent` by design. This
ensures that no other plugin can accidentally or intentionally override our security checks. With `PlayerJoinEvent`,
once we detect restricted permissions and kick the player, the action cannot be cancelled or overridden by other
plugins, providing maximum security guarantee.

**Why not PlayerLoginEvent?** Although `PlayerLoginEvent` would prevent the
player from appearing in the tab list, other plugins with higher priority could potentially `allow()` the connection
after our `disallow()`, creating a security vulnerability.

**AxiomPaper Compatibility:** This plugin may interfere with the AxiomPaper plugin's functionality. AxiomPaper only
checks permissions when a player joins the server, so if you remove all permissions and then restore them while in-game,
the Axiom mod will not work properly. To make PermGuard and Axiom work together, you can grant yourself the `axiom.*`
permission on your account (and configure PermGuard not to remove it) to ensure both plugins function correctly.

PermGuard uses an optimized permission cache built on reload. On join it performs a single fast‑path check for the
wildcard (*) and then scans a deduplicated, immutable list of regular permissions. No parsing or reordering happens on
join.

**Native MiniMessage Support:** Plugin uses only native Kyori Adventure MiniMessage implementation without any
backporting or compatibility layers:

- **Paper 1.18+:** Full native MiniMessage support with all features including gradients, hover effects, click events,
  and advanced formatting
- **Paper 1.16-1.17:** Partial support with automatic conversion to legacy ChatColor codes. Supported features include
  basic colors (`<red>`, `<blue>`, etc.), text styles (`<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`,
  `<obfuscated>`), and reset tags (`<reset>`). Advanced features like gradients and hover effects are automatically
  stripped without causing errors.

You can use the [MiniMessage Web Editor](https://webui.advntr.dev/) to test and preview your formatting. The plugin will
automatically adapt the formatting to your server's capabilities, so you can use the same configuration across different
server versions.

## 📦 Other Plugins

Also check out my other plugins for protecting your Minecraft server:

- [**LeverLock**](https://github.com/alex2276564/LeverLock)  
  *LeverLock* - a plugin to prevent rapid lever interactions, which can cause lag or be exploited for unintended game
  mechanics. Works in conjunction with **AntiRedstoneClock-Remastered**, providing comprehensive protection from
  redstone-based lag and exploits.

- [**NoMoreTNTChainCrash**](https://github.com/alex2276564/NoMoreTNTChainCrash)  
  *NoMoreTNTChainCrash* is a Minecraft plugin designed to prevent server crashes and lag caused by excessive TNT
  explosions. It achieves this by ignoring TNT before automated chain explosions can occur, while still allowing players
  to manually detonate TNT as desired.

> 🔍 **You can find more of my Minecraft plugins here:**  
> [https://github.com/alex2276564?tab=repositories](https://github.com/alex2276564?tab=repositories)

## 🆘 Support

If you encounter any issues or have suggestions for improving the plugin, please create
an [issue](https://github.com/alex2276564/PermGuard/issues) in this repository.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

[Alex] - [https://github.com/alex2276564]

We appreciate your contribution to the project! If you like this plugin, please give it a star on GitHub.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check
the [issues page](https://github.com/alex2276564/PermGuard/issues).

### How to Contribute

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a Pull Request.

---

Thank you for using **PermGuard**! We hope it enhances the security of your Minecraft server significantly, making
unauthorized access and privilege abuse a thing of the past. 🎮🔒
