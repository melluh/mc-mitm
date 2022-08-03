# mc-mitm
A Minecraft proxy used for debugging and testing.
It's like [mitmproxy](https://github.com/mitmproxy/mitmproxy), except for Minecraft.

## Project Goals
- [x] Intercept packets
- [ ] Display packets in a (web?) interface
- [ ] Support joining online-mode servers (requires re-authentication through Microsoft OAuth flow)
- [ ] Multi-version support, automatic version detection
- [ ] Allow searching for a specific packet field value (for example, entity ID)
- [ ] Replay earlier packets, or send new packets to the client or server