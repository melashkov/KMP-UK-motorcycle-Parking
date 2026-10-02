# Release notes and project support

The map overflow menu contains **Add parking bay**, **What’s new**, and
**Behind the app 👋**. These last two open separate full-screen pages.

Only What’s new opens automatically. Its dismissal is stored as `last_seen_release`
in Android SharedPreferences / iOS NSUserDefaults. Back and Continue to map both
mark the current notes as read. Behind the app never appears automatically and
contains the project story and contribution action. Both pages include the optional
“Buy me a coffee on Ko-fi” link.

For a future notable release, update the release strings in `strings.xml` and
change `CurrentReleaseId` in `MapOverflowMenu.kt`. Keep the ID unchanged for builds
with the same notes. The previous combined-message dismissal does not suppress
these new release notes. Clearing app data resets read status.

## Set up your page

The app is configured to open https://ko-fi.com/denys3573.

1. Finish your Ko-fi profile, including your avatar, bio and thank-you message.
2. In Ko-fi Settings → Payment, connect a supported Stripe account or PayPal.
   Ko-fi does not support Stripe Express accounts, such as platform-specific
   payout accounts. Complete payment setup directly with the provider; no payment
   credentials or API keys belong in this repository.
3. Configure optional one-time tips. Do not attach app features, badges,
   exclusive data or other digital benefits to payments.
4. Verify the public page and payment options while signed out.
5. To change the destination later, edit `koFiUrl` in
   `shared/src/commonMain/kotlin/com/melashkov/mcparking/ui/support/SupportConfig.kt`
   and rebuild.

An empty URL hides both the coffee button and its explanatory paragraph. There is
no placeholder payment destination. With a URL configured, the button appears in
the What’s new and Behind the app pages. It opens the
system browser; a launch failure leaves the URL visible so users can visit it.

See Ko-fi's current [payment setup guide](https://help.ko-fi.com/hc/en-us/articles/360007522474-Connect-your-Stripe-account-and-start-earning)
and your account's fee settings. This integration uses a public page link, not a
native payment SDK or webhook.

## Store release note

An external payment button is not automatically eligible merely because it is
called a donation. Before releasing the configured button, check the applicable
storefront rules for developer tips. Google and Apple describe conditions for
optional person-to-person gifts, including funds going to the recipient and no
digital benefits. Eligibility depends on the payment arrangement and storefront;
it should not be assumed. This implementation is a browser link, not an in-app purchase integration.

- [Google Play payments guidance](https://support.google.com/googleplay/android-developer/answer/10281818?hl=en)
- [Apple App Review Guidelines, 3.1.1 and 3.2.1(vii)](https://developer.apple.com/app-store/review/guidelines/)

## Device checks before release

- Launch with no release ID saved: What’s new opens with release notes and the optional Ko-fi link.
- Dismiss and relaunch: it stays dismissed. Reopen it from the menu.
- Behind the app opens only from its menu item and includes the Ko-fi button.
- Add parking bay opens the existing editor at the map centre.
- Open Ko-fi, then return: the story remains open.
- Change CurrentReleaseId for new notes: they appear once again.
- Check small screens, landscape, large text, TalkBack and VoiceOver; content
  scrolls and the map-return button stays available.
