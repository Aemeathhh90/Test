# KakaAnime V1 — Diamond UI Reference

Status: LOCKED
Date: 2026-09-14

## Reference
The user supplied and approved the Diamond UI reference for both Free and Premium states. The approved visual direction is based on the supplied reference images and the generated KakaAnime adaptations shown during this discussion.

## Free User — Diamond Page
The Diamond page keeps the approved layout:
- Dark-first KakaAnime UI.
- Compact top header with KakaAnime branding, Diamond balance pill, search, and notification.
- Page title: `Diamond`.
- Large hero/card showing `Diamond Kamu` and current balance.
- Clear text: `1 Diamond = 1 Episode`.
- Anime artwork integrated into the hero card.
- `Tentang Diamond` information card.
- `Cara Mendapatkan Diamond` information card.
- Bottom promotional artwork/banner may be used as a visual closing element.
- No Diamond Top Up packages.
- No Diamond farming button on this page.

## Free Balance States
### Balance > 0
Show the Diamond icon and current balance prominently.

Example:
`💎 7.848`

### Balance = 0
Keep the same page/layout, but display the zero balance distinctly.

Example:
`💎 0`

The explanation should state that when Diamond is 0, the user will be asked to watch an ad when selecting an episode.

## Premium User — Diamond Page
Premium users do not use Diamond as episode currency.

The same overall visual language/layout can be reused, but the main state changes to:
- Premium User badge.
- `∞ Unlimited` as the primary status.
- `Nonton Tanpa Batas` supporting text.
- Premium benefits section.
- Premium subscription/status section with remaining time, e.g. `27 hari lagi`.
- About Diamond section explains that Premium users have unlimited episode access and do not need Diamonds.

## Header State
Free:
`💎 7.848`

Premium:
`∞ Unlimited`

Do not add a separate Premium button to the Home header.

## Interaction Rules
- Tapping the Free Diamond header control opens the Diamond information page.
- Tapping the Premium `∞ Unlimited` header control opens Premium status/details.
- The Diamond page itself does not start an ad.
- Ads are only triggered when a Free user selects an episode while Diamond = 0.
- If a real rewarded ad is unavailable, the previously locked countdown fallback is used.

## Visual Principles
- Preserve the supplied reference layout rather than inventing a new information architecture.
- Rounded cards, subtle borders, dark background, purple/blue accent glow.
- Large readable balance/status.
- Anime artwork should support the UI without overpowering the information.
- Keep the page informative and uncluttered.
