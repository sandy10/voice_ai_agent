# Agent Coding Guidance

If you are using an AI coding agent, ask it to use Agora skills before changing Agora integration code.

The Agora CLI includes a built-in workflow catalog:

```bash
agora skills list
agora skills search voice
agora skills show <skill-id>
```

For automation, prefer JSON output:

```bash
agora skills list --json
agora introspect --json
```

Use the skill guidance together with this repo's source files so agent-generated changes stay aligned with Agora's current quickstart, env, RTC, RTM, and Conversational AI patterns.
