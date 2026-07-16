package br.com.matheus.commerceapi.initializer;

import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Value("${app.categories.auto-create:true}")
    private boolean autoCreate;

    @Override
    @Transactional
    public void run(String... args) {
        if (!autoCreate) {
            log.info("ℹ️ Auto-creation of default categories is disabled.");
            return;
        }

        if (categoryRepository.count() > 0) {
            log.info("✅ Categories already exist. Total: {}", categoryRepository.count());
            return;
        }

        log.info("🌱 Creating default categories...");
        List<Category> categories = getDefaultCategories();
        categoryRepository.saveAll(categories);

        log.info("✅ Default categories initialized successfully!");
        log.info("📊 {} categories created:", categories.size());
        categories.forEach(category ->
                log.info("  • {} -> '{}' ({})", category.getDisplayName(), category.getName(),
                        category.isActive() ? "ativo" : "inativo")
        );
    }

    private List<Category> getDefaultCategories() {
        return List.of(
                createCategory("Eletrônicos", "Produtos eletrônicos, smartphones, computadores e acessórios"),
                createCategory("Roupas", "Vestuário, calçados e acessórios de moda"),
                createCategory("Alimentos", "Produtos alimentícios, bebidas e mercearia"),
                createCategory("Livros", "Livros, revistas, e-books e publicações"),
                createCategory("Esportes", "Equipamentos esportivos, academia e outdoor"),
                createCategory("Casa e Decoração", "Móveis, decoração, utilidades domésticas"),
                createCategory("Beleza e Cuidados Pessoais", "Cosméticos, perfumes, cuidados com a pele"),
                createCategory("Brinquedos", "Brinquedos, jogos e produtos infantis"),
                createCategory("Automotivo", "Peças, acessórios e equipamentos automotivos"),
                createCategory("Saúde e Bem-estar", "Produtos de saúde, bem-estar e medicamentos"),
                createCategory("Informática", "Computadores, notebooks, periféricos e softwares"),
                createCategory("Jogos", "Video games, jogos de tabuleiro e acessórios")
        );
    }

    private Category createCategory(String displayName, String description) {
        String uniqueName = adaptName(displayName);

        return Category.builder()
                .name(uniqueName)
                .displayName(displayName.trim())
                .description(description.trim())
                .active(true)
                .build();
    }

    /**
     * Adapta um nome de exibição para um identificador único em formato de slug.
     * Remove acentos, substitui espaços por underscores e remove caracteres especiais.
     *
     * @param displayName Nome de exibição da categoria
     * @return Nome adaptado para uso como identificador único
     */
    private String adaptName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return displayName;
        }

        String normalized = Normalizer.normalize(displayName.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        return normalized
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase()
                .replaceAll("_+", "_");
    }
}