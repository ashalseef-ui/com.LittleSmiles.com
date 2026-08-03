const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { google } = require("googleapis");

admin.initializeApp();

/**
 * Verifies a Play Store purchase token server-side and updates the user's Firestore document.
 *
 * Expected data:
 * - purchaseToken: string
 * - productId: string
 * - productType: 'inapp' | 'subs'
 */
exports.verifyPurchase = functions.https.onCall(async (data, context) => {
    // 1. Security Check: Must be logged in
    if (!context.auth) {
        throw new functions.https.HttpsError(
            "unauthenticated",
            "The function must be called while authenticated."
        );
    }

    const { purchaseToken, productId, productType } = data;
    const packageName = "com.LittleSmiles.com"; // Matches Android namespace

    try {
        // 2. Authenticate with Google Play Developer API
        // NOTE: requires service-account-key.json in the functions folder
        const auth = new google.auth.GoogleAuth({
            scopes: ["https://www.googleapis.com/auth/androidpublisher"],
            keyFile: "./service-account-key.json",
        });
        const androidPublisher = google.androidpublisher({
            version: "v3",
            auth,
        });

        let isValid = false;

        // 3. Verify based on product type
        if (productType === "subs") {
            const response = await androidPublisher.purchases.subscriptions.get({
                packageName,
                subscriptionId: productId,
                token: purchaseToken,
            });
            // paymentState 1 = Payment received
            isValid = response.data.paymentState === 1;
        } else {
            const response = await androidPublisher.purchases.products.get({
                packageName,
                productId,
                token: purchaseToken,
            });
            // purchaseState 0 = Purchased
            isValid = response.data.purchaseState === 0;
        }

        // 4. Update Firestore using Admin SDK (bypasses Security Rules)
        if (isValid) {
            await admin
                .firestore()
                .collection("users")
                .document(context.auth.uid)
                .update({
                    isPremium: true,
                    lastVerifiedPurchaseToken: purchaseToken,
                    premiumVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
                });

            return { success: true, message: "Premium status updated." };
        } else {
            return { success: false, error: "Invalid or expired purchase token." };
        }
    } catch (error) {
        console.error("Verification Error:", error);
        throw new functions.https.HttpsError(
            "internal",
            "Failed to verify purchase with Google Play API."
        );
    }
});
