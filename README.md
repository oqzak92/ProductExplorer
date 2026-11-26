# Product Explorer

Application Android (Kotlin + Jetpack Compose) développée progressivement pendant les TP de l'atelier
**Jetpack Compose**. Chaque TP ajoute un écran ou une fonctionnalité au même projet : l'historique
des commits montre l'évolution (un commit par TP).

## Avancement

| TP | Contenu | Statut |
|---|---|---|
| TP4 | Écran de détail produit | ✅ |
| TP5 | Écran d'accueil | ✅ |
| TP6 | Catalogue de produits (`LazyColumn`, `LazyRow`) | ✅ |
| TP7 | Personnalisation graphique (Material 3, thème clair / sombre) | ✅ |
| TP8 | Catalogue interactif (recherche, filtre stock, favoris) | ✅ |
| TP9 | State Hoisting (composants stateful / stateless) | ✅ |
| TP10 | Gestion de l'écran avec un `ViewModel` | ✅ |
| TP11 | Mise à jour dynamique avec `StateFlow` | ✅ |
| Autonomie | Filtre par catégorie, favoris uniquement, tri par prix, écran détail | ✅ |

## Captures

| TP4 – Détail produit | TP5 – Accueil |
|---|---|
| ![Détail produit](screenshots/tp4-detail.png) | ![Accueil](screenshots/tp5-accueil.png) |

| TP9 – Catalogue (clair) | TP9 – Catalogue (sombre) |
|---|---|
| ![Catalogue clair](screenshots/tp9-catalogue-clair.png) | ![Catalogue sombre](screenshots/tp9-catalogue-sombre.png) |

| TP11 – Filtres et tri (autonomie) | TP11 – Thème sombre | Détail ouvert depuis le catalogue |
|---|---|---|
| ![Filtres](screenshots/tp11-catalogue-filtres.png) | ![Sombre](screenshots/tp11-catalogue-sombre.png) | ![Détail](screenshots/autonomie-detail.png) |

## TP4 – Écran de détail produit

`ProductDetailScreen` affiche la fiche d'un produit (`ProductUi`, données locales) :

- `ProductImage` : image du produit ;
- `ProductHeader` : nom, marque et catégorie ;
- `ProductPriceCard` : prix et remise dans une `Card` ;
- `ProductRating` : note avec une `Icon` étoile ;
- `ProductAvailabilityCard` : stock et livraison (affichage différent si le produit est en rupture) ;
- `ProductDescription` : description ;
- `ProductWarrantyField` : garantie dans un `OutlinedTextField` en lecture seule ;
- `AddToCartButton` : bouton « Ajouter au panier » (callback).

## TP5 – Écran d'accueil

`ProductHomeScreen` est la page d'accueil de l'application :

- `HomeHeader` : titre et message d'accueil ;
- `SearchPreviewBar` : aperçu de la barre de recherche ;
- `FeaturedProductSection` + `ProductQuickInfoRow` : produit mis en avant ;
- `CategoriesSection` + `CategoryChip` : catégories ;
- `DailyOfferBox` : offre du jour (`Box` avec éléments superposés).

## TP6 – Catalogue de produits

L'application s'ouvre maintenant sur le catalogue (les écrans des TP4 et TP5 restent dans le code).

- `ProductUi` a un `id`, utilisé comme `key` stable dans la liste ;
- `sampleProducts()` et `sampleCategories()` : données locales ;
- `ProductCatalogScreen` : une seule `LazyColumn` avec des `item { }` (titre, description,
  catégories) et `items(products, key = { it.id })` pour les produits ;
- `CategoryRow` : `LazyRow` qui réutilise `CategoryChip` du TP5 ;
- `ProductListItem` : carte réutilisable pour un produit (titre, marque, prix, note, bouton).

## TP7 – Personnalisation graphique (Material Design 3)

- `Theme.kt` : deux `ColorScheme` (clair et sombre, tons verts) choisis avec
  `isSystemInDarkTheme()` ; les couleurs dynamiques sont retirées pour garder la même identité ;
- `Shapes` personnalisées (`small` 8 dp → `extraLarge` 32 dp) ;
- `Type.kt` : `ProductTypography` (`headlineMedium`, `titleLarge`, `titleMedium`, `bodyMedium`,
  `labelLarge`) ;
- l'écran et les cartes utilisent `MaterialTheme.colorScheme`, `typography` et `shapes`
  (aucune couleur en dur) ; `CategoryChip` devient une pastille `secondary` ;
- previews `Light Theme` et `Dark Theme` pour comparer les deux rendus.

## TP8 – Catalogue interactif (états Compose)

- `searchQuery`, `showOnlyInStock` et `favoriteProductIds` sont des états créés avec
  `mutableStateOf` et conservés avec `rememberSaveable` (ils survivent à une rotation) ;
- `filteredProducts` est calculée avec `remember(products, searchQuery, showOnlyInStock)` : la
  recherche porte sur le titre, la marque et la catégorie ;
- un `OutlinedTextField` pour la recherche, un bouton pour le filtre « en stock », un texte
  « N produit(s) affiché(s) » ;
- favoris : `toggleFavorite()` crée une nouvelle liste à chaque clic pour que Compose détecte le
  changement ; `ProductListItem` reçoit `isFavorite` et `onFavoriteClick`.

## TP9 – State Hoisting

Le code du TP8 est réorganisé en séparant l'état et l'affichage :

- `ProductCatalogContainer` (**stateful**) : possède les états, calcule la liste filtrée et contient
  `toggleFavorite()` ;
- `ProductCatalogScreen` (**stateless**) : ne contient plus de `remember`, il reçoit les valeurs
  (`searchQuery`, `showOnlyInStock`, `favoriteProductIds`, `categories`…) et des callbacks
  (`onSearchQueryChange`, `onToggleStockFilter`, `onFavoriteClick`, `onProductClick`) ;
- `ProductListItem` reste stateless ;
- `MainActivity` fournit les données et appelle `ProductCatalogContainer`.

Principe : **les données descendent, les événements remontent.**

Previews : `ProductCatalogContainerLightPreview`, `ProductCatalogContainerDarkPreview` (écran complet
avec son état) et `ProductCatalogScreenStatelessPreview` (écran stateless avec des valeurs fixes).

## TP10 – ViewModel

L'état du catalogue quitte les composables pour aller dans `ProductCatalogViewModel` :

- le ViewModel fournit les données locales (`allProducts` privé, `categories` exposé) ;
- `searchQuery`, `showOnlyInStock` et `favoriteProductIds` sont des `mutableStateOf` avec
  `private set` : seul le ViewModel peut les modifier, via des fonctions d'événement
  (`onSearchQueryChange`, `onToggleStockFilter`, `onFavoriteClick`) ;
- `filteredProducts` est calculée à partir de l'état ;
- `ProductCatalogContainer` récupère le ViewModel avec `viewModel()` et fait la passerelle vers
  `ProductCatalogScreen` (toujours stateless) ;
- l'état survit à la rotation de l'écran ; les previews utilisent l'écran stateless.

Organisation : `MainActivity` → `ProductCatalogContainer` → `ProductCatalogViewModel` →
`ProductCatalogScreen` → `ProductListItem`.

## TP11 – StateFlow

- `ProductCatalogUiState` (`data class`) regroupe **tout** l'état de l'écran ;
- le ViewModel garde un `MutableStateFlow` privé (`_uiState`) et expose un `StateFlow` en lecture
  seule (`uiState = _uiState.asStateFlow()`) ;
- chaque événement produit un **nouvel état** avec `_uiState.update { it.copy(...) }` ;
- le container observe l'état avec `collectAsState()` : chaque nouvelle valeur recompose l'écran.

Principe : le ViewModel publie un état, Compose l'observe, l'interface se met à jour.

## Fonctionnalités ajoutées en autonomie

Toutes sont intégrées au `StateFlow` (champs ajoutés dans `ProductCatalogUiState`, une fonction
d'événement par action dans le ViewModel) :

- **Filtre par catégorie** : les chips de catégorie sont cliquables (`selectedCategory`), la
  catégorie choisie passe en couleur `primary`, un second clic la désélectionne ;
- **Favoris uniquement** : `FilterChip` (`showOnlyFavorites`) ; retirer un favori pendant que le
  filtre est actif le fait disparaître de la liste ;
- **Tri par prix** : `enum class PriceSort` (aucun → croissant → décroissant) ;
- **Liste vide** : message « Aucun produit ne correspond… » et bouton **Réinitialiser les filtres**
  (les favoris sont conservés) ;
- **Écran détail** : « Voir le produit » ouvre l'écran du TP4 (`selectedProduct` dans l'état) avec
  un bouton « Retour au catalogue » ; le bouton retour d'Android (`BackHandler`) revient aussi au
  catalogue, en gardant les filtres ; la fiche défile (`verticalScroll`) sur les petits écrans.

Tous les filtres sont combinés dans une seule fonction privée du ViewModel
(`withFilteredProducts()`), appelée après chaque changement d'état.

## Notions utilisées

- composants : `Text`, `Button`, `Icon`, `Image`, `Card`, `OutlinedTextField`, `Surface` ;
- layouts : `Column`, `Row`, `Box`, `Spacer` ;
- listes : `LazyColumn`, `LazyRow`, `item`, `items` avec `key` ;
- Material 3 : `ColorScheme`, `Typography`, `Shapes`, thème clair / sombre ;
- état : `mutableStateOf`, `remember`, `rememberSaveable` ;
- State Hoisting : composables stateful / stateless, callbacks ;
- `ViewModel`, `viewModel()`, `StateFlow` / `MutableStateFlow`, `update`, `copy`, `collectAsState()` ;
- `BackHandler`, `FilterChip`, `enum class` ;
- `data class` pour les données du produit, previews.

## Lancer le projet

Ouvrir le dossier dans Android Studio et lancer la configuration `app`
(ou `./gradlew assembleDebug`).

## Organisation

```
app/src/main/java/com/example/productexplorer/
├── MainActivity.kt   // données, UiState, ViewModel, écrans (détail, accueil, catalogue), previews
└── ui/theme/         // Theme.kt (couleurs + formes), Type.kt (typographie)
```

*Ce README est complété à chaque nouveau TP.*
