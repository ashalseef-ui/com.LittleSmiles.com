# Pending Task: Subscription Cycle Implementation

**Status:** ON HOLD (as of Aug 1, 2026)

## Overview
The foundation for the Little Smiles subscription system has been implemented, including the Play Billing integration and Firestore sync. However, the system is not yet "Live" because the Google Play Merchant Account setup is pending.

## Key Reference Documents
* [Subscription PRD](file:///.artifacts/subscription_prd.artifact.md) - Product requirements and user flow.
* [Subscription TRD](file:///.artifacts/subscription_trd.artifact.md) - Technical architecture and database schema.

## Current UI touchpoints (Where "Buy" buttons are)
If we decide to publish a "Free-only" version, these need to be hidden:
1. **Main Menu Banner:** `MenuScreen.kt` - Shows "Upgrade" / "Start Trial" CTAs.
2. **Locked Games:** `MenuScreen.kt` - Animals, Matching, and Drawing are locked behind `AccessTier`.
3. **Paywall Screen:** `UpgradeScreen.kt` - Displays pricing cards and restore options.
4. **Trial Logic:** `AppViewModel.kt` - Calculates if a user is currently in the 7-day trial.

## Blocks / Next Steps
1. **Merchant Account:** Need to link a bank account and tax info in Play Console to enable real product IDs (`premium_monthly`, etc.).
2. **Server-Side Verification:** Implement a Firebase Cloud Function to verify purchase tokens (currently noted as a TODO in `BillingRepositoryImpl`).
3. **Internal Testing:** Once products are created in the console, add testers to verify the "Success" flow without real charges.

## How to Resume
1. Review the [TRD](file:///.artifacts/subscription_trd.artifact.md) for the Firestore schema changes.
2. Complete the Google Play Console Merchant setup.
3. Verify that `BillingRepositoryImpl` can successfully fetch the prices from the store.
