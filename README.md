This mod is an overhaul of the enchanting system to do two major things:
- Make it something to be engaged with heavily throughout the mid-game and lightly in the end-game and
- Connect it more to the world by making it less RNG driven

In pursuit of those goals, this mod introduces two major things **Runes** and **Decorations**.

## Runes
Runes behave like Enchanted Books, storing a single level of a single enchantment where an Enchanted Book could store several enchantments at varying levels. Runes come in 3 different states: Normal, Open, and Charged. Runes also have a Flux value which tracks how likely things are to go wrong... but we'll set that aside for now.

Runes can be obtained in a variety of ways, but most can be crafted using recipes you can learn by finding enchanted books in your world. When crafted, runes will be of the Normal variety and have a Flux value of 8.

Runes can be added to tools and armor -- whether it's already enchanted or not -- in the enchanting table. Enchanting an item with a rune can get you the first level of an enchantment, but combining multiple Normal runes won't get you above first level. To do that, you'll need Open runes.

You can obtain an Open rune of a particular enchantment by breaking an item with that enchantment. A number of Open runes will be dropped according to the level of the enchantment on the item. You can then use these Open runes to enchant to higher levels.

That may have gotten a little technical so lets run through an example. To get a diamond axe enchanted with Unbreaking III you would first need 3 Normal Unbreaking runes. You could then enchant two stone axes and your diamond axe with Unbreaking I. You then use the stone axes until they break, giving you two Open Unbreaking runes. You can then enchant your diamond axe with the Open Unbreaking runes to give you an Unbreaking III axe.

Charged runes work the same as open runes, but are much rarer and much more powerful. They are obtained by breaking a netherite item and allow you to enchant to one level above an enchantments max level. However, unlike Open runes, breaking a netherite item will only ever drop a single Charged rune.

A rune's Flux value will change depending on the material of the tool that Opened it. Wood and stone tools drop high Flux runes, diamond and netherite drop low Flux runes, with copper, iron, and gold tools in between. You can also use diamonds to reduce a runes Flux by combining them in the enchanting table.

## Decorations
Flux measures how likely it is for something to go wrong, but... what could go wrong? When applying an enchantment to an item, there's a chance that it fails. In this case, the runes are consumed, but your item does not recieve the enchantment. Ouch. Especially if you just broke a diamond sword to get that Sharpness rune. But don't worry, you can change the Consequence of enchanting by using Decorations!

Decorations are special blocks that when placed around your enchanting set up will change what happens when enchanting goes wrong. For example, placing down a lighting rod will summon lightning, placing down candles will apply a curse to your item, and more! But be careful. Decorations are consumed when used, so to make sure your runes don't get consumed, double check you have a decoration placed down.

Decorations can be placed within 3 blocks of the enchanting table horizontally and 1 block vertically (giving you a 7x7x3 area). Bookshelves have been changed to fit in a more similar area as well, but we'll cover that later.

### Experience
The way enchanting interacts with experience has also been changed. Rather than losing a level or two every time you enchant, enchanting normally has no level _cost_ however it does have level _requirement_. In other words, you must have a certain number of levels depending on the strength of your enchantment.

This is because, in addition to the enchantment Consequence, when enchanting fails, you lose a number of levels equal to the level requirement.

Decorations don't affect this.

### Bookshelves
Bookshelves also become a much more interactive part of the enchanting experience. Bookshelves now interact with the Flux system by reducing the likelihood for enchanting to fail. Here's a quick rundown of how bookshelves work here.
- Both bookshelves and chiseled bookshelves give bonuses to enchanting
- The bonus given depends on the level of the books stored in the shelf
    - Bookshelf blocks give a small static bonus
    - A normal book in a chiseled bookshelf gives a small bonus
    - A low level book gives slightly better bonus
    - A high level book gives a large bonus
    - The level of an enchanted book is determined by the highest level enchantment stored on the book
- Books within chiseled bookshelves provide an additional bonus if they store an enchantment matching the one currently being applied.
- The bookshelf area has been increased from a 5x5x2 area to a 7x7x2 area and bookshelves no longer require an open path to the enchanting table to provide their bonus

With these changes you can no longer apply enchantments from enchanted books.

## Other Changes
### Anvils
Anvils have been changed in the following ways:
- The material and level cost of repairing an item in an anvil no longer scales with the number of times an item has been repaired.
- Anvils no longer combine enchantments from similar items, or from enchanted books. Anvils do still preserve the enchantments from the item in the first slot.
- 1 repair item always repairs 50% of an item's durability
- Netherite is now repaired using diamonds instead of netherite ingots.

### Villagers
If the villager config mod is installed (recommended), the librarian villager's trades have been tweaked.
- First level:
    - Paper
    - Bookshelf
    - Lantern
- Second level:
    - Inksac
    - Book
    - Nametag
- Third level:
    - Enchanted book
    - Glass
    - Feather
- Fourth level:
    - Book and Quill
    - Clock
    - Compass
- Fifth level:
    - Rune
        - Mending
        - Luck
        - Channeling
        - Frost Walker
        - Sweeping Edge

### Decorations and Their Consequences
- Obsidian: Breaks into crying obsidian
- Skeleton skull: Summons skeletons
- Wither skeleton skull: Summons wither skeletons
- Amethyst cluster: Spreads amethyst to nearby clusters
- Candle: Applies a curse to the item you're enchanting
- Sea lantern: Floods the area and summons guardians
- Gilded Blackstone: Causes an explosion
- Flower Pots: Transforms your enchanting table into a new decorative plant
- Cobwebs: Summons cave spiders
- End rods: Teleports the player
- Glow berries: Spreads moss to nearby bookshelves
- Lightning rod: Summons lightning on the player
- Soul lanterns, torches, and campfires: Applies wither to the player


## Runes With No Crafting Recipe
- Channeling
    - Sold by villagers
    - Found in desert well archaeology loot
- Soul speed
    - Bartered by piglins
- Swift sneak*
    - Found in Ancient Cities
    - *Crafted from echo shards
- Frost Walker
    - Sold by villagers
    - Found in fishing loot
- Quick charge
    - Dropped by pillager captains
- Windburst
    - Found in ominous vault loot
- Sweeping Edge
    - Found in jungle temple loot
    - Sold by villagers
- Mending
    - Found in fishing loot
    - Found in trail ruins archaeology loot
    - Sold by Villagers
- Luck
    - Found in fishing loot
    - Found in archeology loot
    - Found in end city loot
    - Sold by villagers 