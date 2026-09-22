# Migration issue template

The structure `build_issue_graph.py` emits per view issue (it fills this from the behavior spec when
one exists). Adapters embed the screenshot and rewrite `Depends on #N` with real issue numbers.

```markdown
# [<module>] <kind>: <route> (<viewFile>)

## Screenshot
![<slug>](<screenshot-url>)        <!-- asset URL after upload; local path in dry-run -->

## What to implement
- **View:** `secured/registration/wallet/formWallet.xhtml`
- **Kind:** form
- **Proposed Vaadin route:** `wallet/edit`
- **Title:** Wallet

**Fields**
| Label | Component | Required | Validation |
|-------|-----------|----------|------------|
| Name  | TextField | yes      | @NotBlank, max=45 |

**Actions → service calls**
- **Save** → `walletService.save(value)` → list
- **Back** → `—` → list

**Permissions:** WALLET_ACCESS
**i18n keys:** wallet.name, menu.save, menu.back

## Acceptance criteria
- [ ] All form fields present with the correct Vaadin component
- [ ] Field validation matches the entity Bean Validation + xhtml required flags
- [ ] Save/Update calls the correct service method and navigates back on success
- [ ] View + menu entry gated by the original permission
- [ ] Labels resolve via i18n (correct language)
- [ ] Visual parity with the baseline screenshot (Lumo theme differences allowed)
- [ ] App builds and the view passes a manual smoke test
- [ ] Calls `WalletService` methods: save(Wallet)

## Dependencies
- Depends on **#1** (`[foundation] Scaffold Vaadin 25 app + port service layer`)
- Depends on **#7** (`[core] login: login (index)`)
- Depends on **#21** (`[registration] list: wallets (listWallets)`)

---
_labels:_ migration, module:registration, kind:form · _milestone:_ registration · _epic:_ epic:registration
```

## Notes
- **Foundation** and **epic** issues use trimmed variants (no field table / acceptance criteria;
  epics carry a child task-list the adapter fills).
- Acceptance criteria are *derived* from the spec — every field, column, action→service-call,
  validation, permission, and the screenshot becomes a checkable item. Keep them concrete and
  testable so an implementer (human or Claude via `jsf-view-to-vaadin`) knows when the view is done.
- The richer the behavior spec (run `jsf-view-analyze` first), the more precise the issue. Without a
  spec the issue falls back to a kind-based stub and says so.
