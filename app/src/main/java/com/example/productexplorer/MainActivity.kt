package com.example.productexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productexplorer.ui.theme.ProductExplorerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductExplorerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // le container récupère le ViewModel, qui fournit l'état (StateFlow)
                    // le clic sur "Voir le produit" ouvre maintenant le détail (géré par le ViewModel)
                    ProductCatalogContainer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// ---------- Données (locales) ----------

// Les infos d'un produit à afficher (Ui = pour l'interface)
data class ProductUi(
    val id: Int, // identifiant unique, sert de clé dans la liste
    val title: String,
    val brand: String,
    val category: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val warrantyInformation: String,
    val shippingInformation: String
)

// Produit d'exemple
fun sampleProduct(): ProductUi {
    return ProductUi(
        id = 1,
        title = "Smartphone Toto X",
        brand = "TotoTech",
        category = "Smartphones",
        description = "Un smartphone léger avec un écran lumineux, une bonne autonomie et un design moderne.",
        price = 699.99,
        discountPercentage = 12.5,
        rating = 4.6,
        stock = 34,
        warrantyInformation = "Garantie constructeur : 2 ans",
        shippingInformation = "Livraison estimée : 3 à 5 jours ouvrés"
    )
}

// Deuxième produit : en rupture de stock (pour la 2e preview)
fun sampleProductOutOfStock(): ProductUi {
    return ProductUi(
        id = 2,
        title = "Casque Audio Pulse",
        brand = "SoundPeak",
        category = "Audio",
        description = "Un casque confortable conçu pour écouter de la musique, " +
            "suivre des cours en ligne et travailler dans de bonnes conditions.",
        price = 129.99,
        discountPercentage = 8.0,
        rating = 4.2,
        stock = 0,
        warrantyInformation = "Garantie constructeur : 1 an",
        shippingInformation = "Produit temporairement indisponible"
    )
}

// TP11 : tout l'état de l'écran catalogue regroupé dans un seul objet immuable
data class ProductCatalogUiState(
    val products: List<ProductUi> = emptyList(), // liste déjà filtrée, prête à afficher
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val showOnlyInStock: Boolean = false,
    val favoriteProductIds: List<Int> = emptyList(),
    // fonctionnalités ajoutées en autonomie
    val selectedCategory: String? = null, // null = toutes les catégories
    val showOnlyFavorites: Boolean = false,
    val priceSort: PriceSort = PriceSort.NONE,
    val selectedProduct: ProductUi? = null // produit ouvert dans l'écran détail, null = catalogue
)

// Ordre de tri du catalogue
enum class PriceSort(val label: String) {
    NONE("Tri : aucun"),
    ASCENDING("Prix croissant"),
    DESCENDING("Prix décroissant");

    // passe au tri suivant à chaque clic : aucun -> croissant -> décroissant -> aucun
    fun next(): PriceSort = entries[(ordinal + 1) % entries.size]
}

// Liste de produits pour le catalogue (TP6)
fun sampleProducts(): List<ProductUi> {
    return listOf(
        sampleProduct(),
        sampleProductOutOfStock(),
        ProductUi(
            id = 3,
            title = "Montre Connectée FitTime",
            brand = "FitTime",
            category = "Wearables",
            description = "Une montre connectée simple pour suivre l'activité de la journée.",
            price = 89.99,
            discountPercentage = 15.0,
            rating = 4.4,
            stock = 18,
            warrantyInformation = "Garantie constructeur : 2 ans",
            shippingInformation = "Livraison estimée : 2 à 4 jours ouvrés"
        ),
        ProductUi(
            id = 4,
            title = "Enceinte Mini Boom",
            brand = "BoomSound",
            category = "Audio",
            description = "Une petite enceinte pour écouter de la musique chez soi ou en déplacement.",
            price = 59.99,
            discountPercentage = 5.0,
            rating = 4.1,
            stock = 52,
            warrantyInformation = "Garantie constructeur : 1 an",
            shippingInformation = "Livraison estimée : 3 jours ouvrés"
        ),
        ProductUi(
            id = 5,
            title = "Lampe Connectée Lumo",
            brand = "Lumo",
            category = "Maison",
            description = "Une lampe qui se règle depuis le téléphone, avec plusieurs ambiances.",
            price = 39.99,
            discountPercentage = 0.0,
            rating = 4.0,
            stock = 7,
            warrantyInformation = "Garantie constructeur : 2 ans",
            shippingInformation = "Livraison estimée : 2 jours ouvrés"
        )
    )
}

fun sampleCategories(): List<String> {
    return listOf("Smartphones", "Audio", "Wearables", "Maison")
}

// ---------- TP6 : catalogue de produits ----------

// ---------- TP10 / TP11 : ViewModel + StateFlow ----------

// Le ViewModel garde l'état de l'écran catalogue. Il survit à la rotation de l'écran
// et sépare la logique de l'interface.
// TP11 : l'état est exposé avec un StateFlow, un flux qui contient toujours la valeur actuelle.
class ProductCatalogViewModel : ViewModel() {

    private val allProducts: List<ProductUi> = sampleProducts()
    private val allCategories: List<String> = sampleCategories()

    // MutableStateFlow privé : seul le ViewModel peut modifier l'état
    private val _uiState = MutableStateFlow(
        ProductCatalogUiState(
            products = allProducts,
            categories = allCategories
        )
    )

    // StateFlow public en lecture seule : l'interface l'observe
    val uiState: StateFlow<ProductCatalogUiState> = _uiState.asStateFlow()

    // Recalcule la liste affichée à partir de TOUS les filtres de l'état.
    // Chaque événement modifie l'état puis appelle cette fonction : un seul endroit pour la logique.
    private fun ProductCatalogUiState.withFilteredProducts(): ProductCatalogUiState {
        val filtered = allProducts.filter { product ->
            val matchesSearch =
                product.title.contains(searchQuery, ignoreCase = true) ||
                    product.brand.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)

            val matchesStock = !showOnlyInStock || product.stock > 0
            val matchesCategory = selectedCategory == null || product.category == selectedCategory
            val matchesFavorites = !showOnlyFavorites || favoriteProductIds.contains(product.id)

            matchesSearch && matchesStock && matchesCategory && matchesFavorites
        }

        val sorted = when (priceSort) {
            PriceSort.NONE -> filtered
            PriceSort.ASCENDING -> filtered.sortedBy { it.price }
            PriceSort.DESCENDING -> filtered.sortedByDescending { it.price }
        }

        return copy(products = sorted)
    }

    // update + copy : on crée un nouvel état à partir de l'ancien (jamais modifié directement)
    fun onSearchQueryChange(newValue: String) {
        _uiState.update { it.copy(searchQuery = newValue).withFilteredProducts() }
    }

    fun onToggleStockFilter() {
        _uiState.update { it.copy(showOnlyInStock = !it.showOnlyInStock).withFilteredProducts() }
    }

    fun onFavoriteClick(productId: Int) {
        _uiState.update { currentState ->
            val newFavoriteIds = if (currentState.favoriteProductIds.contains(productId)) {
                currentState.favoriteProductIds - productId
            } else {
                currentState.favoriteProductIds + productId
            }
            // on refiltre : avec "Favoris uniquement", retirer un favori le fait disparaître
            currentState.copy(favoriteProductIds = newFavoriteIds).withFilteredProducts()
        }
    }

    // Autonomie : filtre par catégorie (re-cliquer sur la catégorie choisie la désélectionne)
    fun onCategoryClick(category: String) {
        _uiState.update { currentState ->
            val newCategory = if (currentState.selectedCategory == category) null else category
            currentState.copy(selectedCategory = newCategory).withFilteredProducts()
        }
    }

    // Autonomie : filtre "Favoris uniquement"
    fun onToggleFavoritesFilter() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites).withFilteredProducts() }
    }

    // Autonomie : tri par prix
    fun onPriceSortClick() {
        _uiState.update { it.copy(priceSort = it.priceSort.next()).withFilteredProducts() }
    }

    // Autonomie : ouvrir / fermer l'écran détail d'un produit
    fun onProductClick(product: ProductUi) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun onBackToCatalog() {
        _uiState.update { it.copy(selectedProduct = null) }
    }

    // Autonomie : remet tous les filtres à zéro (les favoris sont conservés)
    fun onResetFilters() {
        _uiState.update { currentState ->
            ProductCatalogUiState(
                categories = allCategories,
                favoriteProductIds = currentState.favoriteProductIds
            ).withFilteredProducts()
        }
    }
}

// Le container observe le StateFlow et transmet l'état à l'écran stateless
@Composable
fun ProductCatalogContainer(
    modifier: Modifier = Modifier,
    viewModel: ProductCatalogViewModel = viewModel()
) {
    // collectAsState : chaque nouvelle valeur du StateFlow provoque une recomposition
    val uiState by viewModel.uiState.collectAsState()

    // Autonomie : si un produit est sélectionné, on affiche son détail (écran du TP4)
    val selectedProduct = uiState.selectedProduct
    if (selectedProduct != null) {
        // le bouton retour d'Android revient au catalogue au lieu de quitter l'app
        BackHandler(onBack = viewModel::onBackToCatalog)
        ProductDetailWithBack(
            product = selectedProduct,
            onBackClick = viewModel::onBackToCatalog,
            modifier = modifier
        )
        return
    }

    ProductCatalogScreen(
        products = uiState.products,
        categories = uiState.categories,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        showOnlyInStock = uiState.showOnlyInStock,
        onToggleStockFilter = viewModel::onToggleStockFilter,
        favoriteProductIds = uiState.favoriteProductIds,
        onFavoriteClick = viewModel::onFavoriteClick,
        selectedCategory = uiState.selectedCategory,
        onCategoryClick = viewModel::onCategoryClick,
        showOnlyFavorites = uiState.showOnlyFavorites,
        onToggleFavoritesFilter = viewModel::onToggleFavoritesFilter,
        priceSort = uiState.priceSort,
        onPriceSortClick = viewModel::onPriceSortClick,
        onResetFilters = viewModel::onResetFilters,
        onProductClick = viewModel::onProductClick,
        modifier = modifier
    )
}

// Autonomie : écran détail du TP4 avec un bouton pour revenir au catalogue
@Composable
fun ProductDetailWithBack(
    product: ProductUi,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = "← Retour au catalogue")
        }
        ProductDetailScreen(
            product = product,
            onAddToCartClick = {
                // Plus tard : ajouter au panier
            }
        )
    }
}

// Composable STATELESS : pas de remember ici, il affiche ce qu'il reçoit et signale les événements
@Composable
fun ProductCatalogScreen(
    products: List<ProductUi>, // liste déjà filtrée par le container
    categories: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyInStock: Boolean,
    onToggleStockFilter: () -> Unit,
    favoriteProductIds: List<Int>,
    onFavoriteClick: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    modifier: Modifier = Modifier,
    // autonomie : valeurs par défaut pour que les previews restent simples
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {},
    showOnlyFavorites: Boolean = false,
    onToggleFavoritesFilter: () -> Unit = {},
    priceSort: PriceSort = PriceSort.NONE,
    onPriceSortClick: () -> Unit = {},
    onResetFilters: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // fond qui vient du thème
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Catalogue produits",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Text(
                text = "Explorez une sélection de produits disponibles localement.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            // le champ ne sait pas où est stockée la recherche : il affiche et signale
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = {
                    Text(text = "Rechercher un produit")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(onClick = onToggleStockFilter) {
                Text(
                    text = if (showOnlyInStock) {
                        "Afficher tous les produits"
                    } else {
                        "Afficher uniquement les produits en stock"
                    }
                )
            }
        }
        item {
            // autonomie : filtre favoris + tri par prix
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = onToggleFavoritesFilter,
                    label = { Text(text = "Favoris uniquement") }
                )
                OutlinedButton(onClick = onPriceSortClick) {
                    Text(text = priceSort.label)
                }
            }
        }
        item {
            CategoryRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategoryClick = onCategoryClick
            )
        }
        item {
            Text(
                text = "${products.size} produit(s) affiché(s)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // autonomie : message quand aucun produit ne correspond aux filtres
        if (products.isEmpty()) {
            item {
                EmptyCatalogMessage(onResetFilters = onResetFilters)
            }
        }
        // une carte par produit, l'id sert de clé stable
        items(
            items = products,
            key = { product -> product.id }
        ) { product ->
            ProductListItem(
                product = product,
                isFavorite = favoriteProductIds.contains(product.id),
                onFavoriteClick = {
                    onFavoriteClick(product.id)
                },
                onClick = {
                    onProductClick(product)
                }
            )
        }
    }
}

// Catégories sur une ligne qui défile horizontalement
@Composable
fun CategoryRow(
    categories: List<String>,
    modifier: Modifier = Modifier,
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                label = category,
                isSelected = category == selectedCategory,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

// Autonomie : affiché quand les filtres ne laissent aucun produit
@Composable
fun EmptyCatalogMessage(
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Aucun produit ne correspond à votre recherche.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onResetFilters) {
            Text(text = "Réinitialiser les filtres")
        }
    }
}

// Carte réutilisable pour un produit de la liste
@Composable
fun ProductListItem(
    product: ProductUi,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // forme, couleur et élévation viennent du thème
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = product.brand,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // prix + note mis en valeur avec la couleur primary
            Text(
                text = "${product.price} € • ★ ${product.rating}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick) {
                Text(text = "Voir le produit")
            }
            // la carte ne connaît pas la liste des favoris : elle affiche l'info et signale le clic
            Button(onClick = onFavoriteClick) {
                Text(
                    text = if (isFavorite) {
                        "Retirer des favoris"
                    } else {
                        "Ajouter aux favoris"
                    }
                )
            }
        }
    }
}

// ---------- TP4 : écran de détail produit ----------

// L'écran détail : il assemble juste les blocs
@Composable
fun ProductDetailScreen(
    product: ProductUi,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // la fiche défile sur les petits écrans
            .padding(16.dp)
    ) {
        ProductImage(
            productTitle = product.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        ProductHeader(
            title = product.title,
            brand = product.brand,
            category = product.category
        )

        ProductPriceCard(
            price = product.price,
            discountPercentage = product.discountPercentage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        ProductRating(
            rating = product.rating,
            modifier = Modifier.padding(top = 16.dp)
        )

        ProductAvailabilityCard(
            stock = product.stock,
            shippingInformation = product.shippingInformation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        ProductDescription(
            description = product.description,
            modifier = Modifier.padding(top = 16.dp)
        )

        ProductWarrantyField(
            warrantyInformation = product.warrantyInformation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        AddToCartButton(
            onClick = onAddToCartClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

// Nom, marque, catégorie (reçoit seulement ce dont il a besoin, pas tout le produit)
@Composable
fun ProductHeader(
    title: String,
    brand: String,
    category: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = brand,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Prix + remise dans une Card
@Composable
fun ProductPriceCard(
    price: Double,
    discountPercentage: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) { // padding interne
            Text(
                text = "$price €",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Remise : $discountPercentage %",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Image placeholder fournie par Android
@Composable
fun ProductImage(
    productTitle: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = android.R.drawable.ic_menu_gallery),
        contentDescription = "Image du produit $productTitle", // pour TalkBack
        modifier = modifier
    )
}

// Étoile + note sur une même ligne
@Composable
fun ProductRating(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Note du produit"
        )
        Text(
            text = "$rating / 5",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// Stock + livraison dans une 2e Card
@Composable
fun ProductAvailabilityCard(
    stock: Int,
    shippingInformation: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Disponibilité",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Stock : $stock unités",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = shippingInformation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Titre "Description" + le texte
@Composable
fun ProductDescription(
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Champ texte en lecture seule pour la garantie
@Composable
fun ProductWarrantyField(
    warrantyInformation: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = warrantyInformation,
        onValueChange = {},   // rien : on ne gère pas encore la saisie
        readOnly = true,
        label = {
            Text(text = "Garantie")
        },
        modifier = modifier
    )
}

// Le bouton signale le clic, c'est le parent qui décide quoi faire
@Composable
fun AddToCartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = "Ajouter au panier")
    }
}

// ---------- TP5 : écran d'accueil ----------

// L'accueil : une Column qui assemble les sections, avec des Spacer entre elles
@Composable
fun ProductHomeScreen(
    featuredProduct: ProductUi,
    onFeaturedProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HomeHeader()
        Spacer(modifier = Modifier.height(24.dp))
        SearchPreviewBar()
        Spacer(modifier = Modifier.height(24.dp))
        FeaturedProductSection(
            product = featuredProduct,
            onClick = onFeaturedProductClick
        )
        Spacer(modifier = Modifier.height(24.dp))
        CategoriesSection()
        Spacer(modifier = Modifier.height(24.dp))
        DailyOfferBox()
    }
}

// Titre + sous-titre en haut de l'accueil
@Composable
fun HomeHeader(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Product Explorer",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Découvrez les produits du moment",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Fausse barre de recherche (juste visuelle, pas de saisie)
@Composable
fun SearchPreviewBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Text(
            text = "Rechercher un produit...",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Bloc du produit mis en avant + bouton "Voir le produit"
@Composable
fun FeaturedProductSection(
    product: ProductUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Produit mis en avant",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = product.brand,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProductQuickInfoRow(
                price = product.price,
                rating = product.rating,
                stock = product.stock
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick) {
                Text(text = "Voir le produit")
            }
        }
    }
}

// Prix, note, stock côte à côte : weight(1f) = une part égale chacun
@Composable
fun ProductQuickInfoRow(
    price: Double,
    rating: Double,
    stock: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$price €",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "★ $rating",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$stock en stock",
            modifier = Modifier.weight(1f)
        )
    }
}

// Une catégorie dans un petit bloc
@Composable
fun CategoryChip(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null // null = chip simple, non cliquable (accueil du TP5)
) {
    // la Surface donne le fond, le Text prend automatiquement contentColor
    // autonomie : la catégorie sélectionnée prend la couleur primary
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// Titre + 3 catégories sur une ligne (les Spacer weight les écartent)
@Composable
fun CategoriesSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Catégories",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            CategoryChip(label = "Smartphones")
            Spacer(modifier = Modifier.weight(1f))
            CategoryChip(label = "Audio")
            Spacer(modifier = Modifier.weight(1f))
            CategoryChip(label = "Maison")
        }
    }
}

// Box : le badge "Nouveau" est posé PAR-DESSUS le bloc, en haut à droite
@Composable
fun DailyOfferBox(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Offre du jour",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Jusqu’à -20 % sur une sélection de produits",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 6.dp
        ) {
            Text(
                text = "Nouveau",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// ---------- Previews ----------

// TP10 : les previews utilisent l'écran stateless (pas de ViewModel dans une preview)
@Preview(
    showBackground = true,
    name = "Catalogue - Light Theme"
)
@Composable
fun ProductCatalogScreenLightPreview() {
    ProductExplorerTheme(darkTheme = false) {
        ProductCatalogScreen(
            products = sampleProducts(),
            categories = sampleCategories(),
            searchQuery = "",
            onSearchQueryChange = {},
            showOnlyInStock = false,
            onToggleStockFilter = {},
            favoriteProductIds = listOf(1),
            onFavoriteClick = {},
            onProductClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Catalogue - Dark Theme"
)
@Composable
fun ProductCatalogScreenDarkPreview() {
    ProductExplorerTheme(darkTheme = true) {
        ProductCatalogScreen(
            products = sampleProducts().filter { it.stock > 0 },
            categories = sampleCategories(),
            searchQuery = "",
            onSearchQueryChange = {},
            showOnlyInStock = true,
            onToggleStockFilter = {},
            favoriteProductIds = listOf(3),
            onFavoriteClick = {},
            onProductClick = {}
        )
    }
}

// L'écran stateless peut aussi s'afficher seul, en lui donnant directement les valeurs
@Preview(showBackground = true)
@Composable
fun ProductCatalogScreenStatelessPreview() {
    ProductExplorerTheme {
        ProductCatalogScreen(
            products = sampleProducts(),
            categories = sampleCategories(),
            searchQuery = "",
            onSearchQueryChange = {},
            showOnlyInStock = false,
            onToggleStockFilter = {},
            favoriteProductIds = listOf(1),
            onFavoriteClick = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductHomeScreenPreview() {
    ProductExplorerTheme {
        ProductHomeScreen(
            featuredProduct = sampleProduct(),
            onFeaturedProductClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    ProductExplorerTheme {
        ProductDetailScreen(
            product = sampleProduct(),
            onAddToCartClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenOutOfStockPreview() {
    ProductExplorerTheme {
        ProductDetailScreen(
            product = sampleProductOutOfStock(),
            onAddToCartClick = {}
        )
    }
}
