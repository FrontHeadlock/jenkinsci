# Jenkins-like CI Platform Blueprint

This document is a **decision-ready blueprint** for building a Jenkins-like CI platform.
It focuses on architecture options, core components, an end-to-end execution flow, a phased roadmap, and the operational/security considerations you must bake in from day one.

## Goals and Non-goals

### Goals

- Build an MVP that can: accept SCM events, schedule runs, execute jobs in an isolated runner, stream logs, store artifacts, and report status.
- Keep the design extensible (hooks/plugins) without over-engineering the MVP.
- Treat user-submitted code as **untrusted** by default (threat model first).

### Non-goals

- This is **not** an implementation guide or deployment runbook.
- This does not prescribe a single vendor stack; it documents tradeoffs.
- This does not define a full database schema; it stays at conceptual entities and lifecycle.

## Glossary

- **Project/Repo**: A unit of source code that triggers CI runs.
- **Pipeline**: A definition of steps to execute for a project (YAML/script/GUI-defined).
- **Run**: One execution instance of a pipeline.
- **Job**: A schedulable unit within a run (often one stage or one matrix cell).
- **Step**: A single command/action executed in a job.
- **Scheduler/Queue**: The system that orders and dispatches jobs.
- **Runner/Worker**: The execution environment for jobs (VM, container, pod, etc.).
- **Artifact**: Output produced by a run (binaries, test reports, coverage, logs snapshots).
- **Secret**: Credentials or tokens used during execution (must be scoped and masked).
- **Trust boundary (신뢰 경계)**: Where untrusted code meets privileged platform resources.

## Conceptual Model

At a high level, your CI platform is a control-plane that receives events and makes scheduling decisions, plus a data-plane that executes untrusted workloads in isolated environments.

### Key assumptions

- Execution code is untrusted (including external PRs).
- Runners must be isolated from secrets, the control-plane, and other tenants/projects.
- Logs and artifacts are security-sensitive data and must be access-controlled and retained safely.

## Architecture Options

## 아키텍처 옵션

This section compares three implementation _shapes_ for a Jenkins-like CI platform.
All three can deliver the same user-facing features; the difference is where you place control-plane responsibilities, how you scale execution, and how you enforce isolation.

### A) Single-server (learning-first)

**A) When to choose**

- You are building a learning MVP and want the shortest path to "it runs".
- You can tolerate limited concurrency (a small number of parallel jobs).
- You want to debug the full system in one process before splitting components.

**A) Pros**

- Minimal moving parts: easiest to reason about and iterate.
- Lower operational overhead (one deployment, one database).
- Great for validating the _product surface_: UI, logs, artifacts, pipeline UX.

**A) Cons**

- Weak isolation by default; easy to accidentally leak secrets or host access.
- Scaling is coarse: vertical scaling and limited concurrency.
- A single failure domain (crash/GC pause) impacts the whole platform.

**A) Failure modes / pitfalls**

- Executor starvation: a few long runs block everyone.
- "MVP shortcuts" become security debt (local execution, shared workspace).

### B) Control-plane + Workers (general-purpose, recommended)

**B) When to choose**

- You need multiple runners and predictable concurrency controls.
- You want isolation to be a first-class boundary (untrusted code vs platform).
- You expect to grow beyond a single machine (multiple teams, more repos).

**B) Pros**

- Clear separation of concerns: scheduling vs execution.
- Horizontal scaling: add workers without changing the control-plane.
- Stronger security posture: workers can be "dumb" and tightly sandboxed.

**B) Cons**

- More components to operate (queue, worker fleet, artifact/log stores).
- Requires reliable messaging and idempotent orchestration.
- Debugging is more distributed (you need good observability).

**B) Failure modes / pitfalls**

- Duplicate execution if dispatch is not idempotent (at-least-once delivery).
- Queue overload without backpressure (run storms from SCM events).

**권장**: Start MVP with **B)** if you want a realistic path to production.
If you are strictly learning, start with **A)**, but design your interfaces as if you will later become **B)**.

**신뢰 경계 / 위협 모델** (Threat model): treat the code being executed as **untrusted** (including external PRs).
That means the runner environment must be isolated from secrets, the control-plane, and other tenants/projects.
The most important early decisions are: execution isolation (container/VM/pod), secret delivery model, and network policy.

### C) Kubernetes-native (controller + CRDs; Tekton-style)

**C) When to choose**

- Your org already runs Kubernetes as the default compute substrate.
- You want native scheduling, autoscaling, and resource isolation primitives.
- You want runs/jobs to be declarative resources (good auditability).

**C) Pros**

- Strong isolation and resource controls via Kubernetes primitives.
- Autoscaling and fleet management become easier (pods as workers).
- Integrates well with cloud-native observability and policy tooling.

**C) Cons**

- Higher upfront complexity (CRDs/controllers, cluster ops, RBAC).
- Multi-tenancy and secret delivery become cluster-wide security concerns.
- Debugging often requires Kubernetes literacy.

**C) Failure modes / pitfalls**

- Noisy-neighbor issues if quotas/limits are not enforced.
- Too much coupling to Kubernetes API semantics if you later want portability.

## Core Components

## 핵심 컴포넌트

Below is a practical decomposition you can implement in multiple architectures (A/B/C). Each item states (1) responsibility and (2) a key design decision you must make early.

- **API + UI**: CRUD for projects/pipelines/runs; show run timelines; stream logs; expose artifacts. Key decision: API shape (REST vs GraphQL) and pagination/streaming strategy.
- **AuthN + AuthZ (RBAC)**: authenticate users/machines and authorize actions (view logs, rerun, manage secrets). Key decision: identity provider integration (OIDC/SAML) and permission model (project/team scopes).
- **SCM Integration**: connect repos, register webhooks, map commits/PRs to runs. Key decision: webhook-only vs polling fallback; how to handle forks/untrusted PRs.
- **Webhook Receiver + Verification**: receive events, validate signature, dedupe/replay-protect. Key decision: canonical verification (HMAC + timestamp) and idempotency keys.
- **Scheduler + Queue**: order work, enforce concurrency limits, apply priorities/fairness, backpressure. Key decision: queue model (FIFO vs priority) and fairness (per-project/tenant quotas).
- **Run Orchestrator**: expand pipeline definition into jobs/steps; compute DAG; handle retries/timeouts/cancellation. Key decision: representation (DAG vs linear stages) and state machine semantics.
- **Runner/Worker Fleet Manager**: manage worker lifecycle (register, health, drain, autoscale). Key decision: static fleet vs autoscaling; ephemeral vs long-lived workers.
- **Execution Sandbox**: the isolation boundary where untrusted code runs. Key decision: isolation level (container/VM/pod) and what network/filesystem access is allowed.
- **Workspace Storage**: source checkout and intermediate files. Key decision: per-run isolated workspace vs shared cacheable workspace; cleanup guarantees.
- **Logs System (streaming + retention)**: ingest live logs, persist, redact secrets, enforce retention. Key decision: append-only log store vs DB; how to tail efficiently.
- **Artifact Store (blobs + metadata)**: store binaries and test reports; provide signed download URLs; enforce retention. Key decision: object storage vs filesystem; metadata model.
- **Cache (dependency/build)**: speed up builds while preventing cache poisoning/exfiltration. Key decision: per-project cache namespaces and read/write policies.
- **Secrets Delivery**: inject secrets safely into runs. Key decision: delivery model (mounted files vs env vars vs sidecar) and scope (job-level, step-level).
- **Notifications**: status updates to PRs/Slack/email/webhooks. Key decision: event model and retries/idempotency.
- **Observability + Audit**: metrics/traces/logs for the platform itself; audit log for sensitive actions. Key decision: what is audited and retention/access controls.
- **Extensibility (plugins/hooks)**: add steps, integrations, UI panels. Key decision: plugin API boundary and sandboxing third-party extensions.

## End-to-End Flow

## 실행 흐름(End-to-End)

The happy-path lifecycle for a single run:

1. **Event arrives** (webhook or polling) with commit/PR metadata.
2. **Validate + normalize**: verify webhook signature, validate payload, compute idempotency key.
3. **Create Run record**: store `Run` + initial status; link to Project/Repo and commit.
4. **Resolve pipeline definition**: load pipeline (YAML 권장) at the commit; expand into a job DAG.
5. **Enqueue jobs**: push `Job` items into the queue with priority, required labels, and resource requests.
6. **Schedule**: scheduler selects an eligible worker (capacity, labels, quotas) and issues a lease.
7. **Provision sandbox**: create the isolated execution environment (container/VM/pod), attach workspace.
8. **Checkout + prepare**: fetch code; restore caches; materialize config; verify toolchain.
9. **Inject secrets safely**: deliver only scoped secrets; ensure masking/redaction is active.
10. **Execute steps**: run commands; stream logs; emit step-level status and timings.
11. **Publish outputs**: upload artifacts and test reports; compute metadata; optionally sign artifacts.
12. **Finalize**: mark Job + Run final status; post SCM status checks; send notifications.
13. **Cleanup**: revoke credentials, delete workspace/sandbox, enforce retention/garbage collection.

### Conceptual Data Model (entities, not a schema)

- **Project/Repo**: contains Pipeline definitions and policies.
- **Pipeline**: versioned definition at a commit; expands into a DAG.
- **Run**: one pipeline execution (commit, trigger, timestamps, overall status).
- **Job**: a schedulable unit (labels, resources, status, assigned worker).
- **Step**: command/action within a job (exit code, logs span, duration).
- **Worker**: execution capacity with capabilities (labels, isolation type, region).
- **Artifact**: blob + metadata (type, checksum, signed URL, retention).
- **SecretRef**: reference to a secret with scope and access policy.

## Phased Roadmap

## 단계별 로드맵

The roadmap below is designed to be learning-friendly (MVP) and still leave a credible path to production.
Each phase has an explicit "include" and "exclude" boundary to prevent scope creep.

### MVP (1-2 weeks)

- Include: webhook trigger + signature validation, run creation, queue, single worker, basic logs streaming, status reporting to SCM.
- Exclude: multi-tenant isolation, advanced caching, artifact UI, HA.

### v0.2

- Include: container-based isolation (at least Docker), per-run workspace cleanup, basic cache namespace, artifact upload (object storage).
- Exclude: distributed worker fleet, quotas/fairness scheduling.

### v0.3

- Include: control-plane + multiple workers, concurrency limits, retries/timeouts/cancel, backpressure and queue health metrics.
- Exclude: approvals/promotions, multi-region HA.

### v0.4

- Include: test reports (JUnit) + artifacts browser, environment concept (dev/stage/prod), deployment mutex/resource group.
- Exclude: full plugin marketplace, enterprise multi-tenancy.

### v1.0

- Include: hardening for production (audit logs, secret rotation, HA strategy, backup/restore, policy enforcement) and optional multi-tenancy.
- Exclude: vendor-specific full-stack IaC runbooks (keep blueprint-level).

## Feature Catalog

## 기능 카탈로그

### Pipeline definition

- **권장**: YAML-based pipeline definition for MVP (low barrier, easy validation). Later, consider script extensions.

### Triggers

- [MVP] SCM webhook triggers (push/PR)
- [Later] scheduled triggers (cron)
- [Later] manual triggers with parameters

### Pipeline structure

- [MVP] stages + jobs + steps (linear or simple DAG)
- [Later] matrix builds, conditional execution, fan-in/fan-out DAG

### Execution control

- [MVP] timeouts, cancellation, basic retries
- [Later] concurrency groups / resource locks, priority queues, fairness quotas

### Outputs

- [MVP] live logs + log retention
- [v0.2] artifact upload + retention policy
- [v0.4] JUnit/test reports, coverage, annotations

### Security

- [MVP] webhook signature verification, secret masking in logs
- [v0.2] scoped secrets delivery model, runner isolation baseline
- [v1.0] audit logs, policy enforcement, supply-chain provenance

### Operations

- [MVP] basic metrics (queue depth, run duration)
- [Later] autoscaling workers, cost controls, SLO dashboards

## Considerations Checklist

## 고려사항 체크리스트

### Security

- Treat executed code as untrusted; runners are the primary trust boundary.
- Untrusted PR policy: fork/external PR runs must have no repository/environment secrets, no write-scoped SCM token, no deployment credentials, and no access to shared mutable caches; require maintainer approval for first-time contributors.
- Token defaults: per-job ephemeral tokens, minimum scopes, separate read-only tokens for PR validation; never expose control-plane service tokens to runners.
- Secrets delivery: prefer short-lived credentials (OIDC federation / STS) over long-lived secrets; scope to job/step; assume log masking can be bypassed and prevent exfil via policy.
- Runner isolation constraints (normative): one job per instance (ephemeral), rootless where possible, no privileged mode, no Docker socket, no hostPath mounts, and guaranteed teardown + credential revocation on completion/failure.
- Network egress: deny-by-default for untrusted jobs; allowlist required endpoints only (SCM, package mirrors); block cloud metadata endpoints and control-plane internal networks.
- Cache poisoning controls: partition caches by trust level (trusted vs untrusted), repo, and toolchain/lockfile hash; untrusted PRs may read approved base caches but cannot write caches consumed by trusted workflows.
- Artifacts integrity: immutable uploads with checksums; trusted release workflows require provenance attestation (e.g., SLSA/in-toto/Sigstore) and policy verification before promote/deploy.
- Webhook anti-replay: validate signature + timestamp window, enforce event-id/nonce uniqueness with TTL store, and process events idempotently.
- RBAC: separate roles for pipeline edit, secrets admin, runner fleet admin, artifact access, and untrusted-run approvals; enforce least privilege.
- Audit logs: append-only/tamper-evident; include secret reads, token minting, permission changes, webhook validation outcomes, reruns/approvals, artifact downloads/promotions, and policy overrides.

### Reliability

- Idempotency: event ingestion, scheduling, and worker dispatch must tolerate retries.
- Backpressure: define what happens when queue depth spikes (drop? delay? limit triggers?).
- Timeouts/cancellation: propagate cancellation to workers and ensure cleanup.
- Recovery: persist run state so control-plane restarts do not corrupt runs.

### Scalability

- Scheduling: design for fairness (per-project/tenant limits) to avoid starvation.
- Worker fleet: autoscale based on queue depth and resource demand.
- Storage bottlenecks: artifacts/logs/caches can dominate cost and performance.

### Observability

- Platform metrics: queue depth, scheduling latency, worker utilization, run duration distribution.
- Logs/traces: trace a run from webhook -> queue -> worker execution -> artifact upload.
- Audit logs: record sensitive actions (secret access, permission changes, artifact downloads).

### Operations

- Retention: define run/log/artifact retention and garbage collection.
- Upgrades: version workers and pipeline runtime; support gradual rollout.
- Backup/restore: metadata store and critical configuration.

## References

## 참고자료

| Topic                              | URL                                                                              | Why it matters                                           |
| ---------------------------------- | -------------------------------------------------------------------------------- | -------------------------------------------------------- |
| Jenkins scaling                    | https://www.jenkins.io/doc/book/scaling/architecting-for-scale/                  | Canonical controller/agent separation and scale patterns |
| Jenkins nodes/agents               | https://www.jenkins.io/doc/book/managing/nodes/                                  | Agent lifecycle and scheduling constraints               |
| Jenkins queue state machine        | https://javadoc.jenkins-ci.org/hudson/model/Queue.html                           | Concrete queue stages and lifecycle concepts             |
| Kubernetes scheduling framework    | https://kubernetes.io/docs/reference/scheduling/config/                          | Scheduler extension points and decision pipeline         |
| Kubernetes pod priority/preemption | https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/ | Priority, preemption, and starvation mitigation patterns |
| GitHub runners                     | https://docs.github.com/en/actions/concepts/runners                              | Runner types and grouping concepts                       |
| GitHub self-hosted runners         | https://docs.github.com/en/actions/hosting-your-own-runners                      | Runner lifecycle and operational concerns                |
| GitLab executors                   | https://docs.gitlab.com/runner/executors/                                        | Isolation models and executor tradeoffs                  |
| GitLab runner concurrency          | https://docs.gitlab.com/runner/configuration/advanced-configuration/             | Concurrency limits and throttling knobs                  |
| Tekton architecture                | https://tekton.dev/docs/pipelines/architecture/                                  | Kubernetes-native control loop model                     |
| Argo Workflows architecture        | https://argo-workflows.readthedocs.io/en/latest/architecture/                    | Controller/server model and pod execution patterns       |
| OWASP CI/CD Security               | https://cheatsheetseries.owasp.org/cheatsheets/CI_CD_Security_Cheat_Sheet.html   | Practical CI/CD security baseline                        |
| OWASP CI/CD risks                  | https://owasp.org/www-project-top-10-ci-cd-security-risks                        | CI/CD-specific threat categories and language            |
| SLSA spec                          | https://slsa.dev/spec/v1.2/                                                      | Build provenance and supply-chain integrity framework    |
| Sigstore docs                      | https://docs.sigstore.dev/                                                       | Signing and verification model for artifacts             |
| NIST SSDF                          | https://csrc.nist.gov/pubs/sp/800/218/final                                      | Secure software development practices (policy anchor)    |
