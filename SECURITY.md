# Security Policy

## Threat model

PermGuard is designed to protect against in-game permission abuse.

## Symlinks in the data directory

By design, PermGuard **follows symbolic links** inside its data folder for:

- configuration files
- backups (the `backups/` directory)

This allows advanced setups where configs are shared between multiple servers
(e.g. via Docker volumes or symlinked config files).

If you do not want data from outside the plugin's data folder to end up in backups,
**do not place symlinks** in the `plugins/PermGuard/` directory.
