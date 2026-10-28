package dev.mmatokovic.pantrio.domain.item;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemShould {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 12, 0);
    private static final LocalDateTime LATER = NOW.plusHours(1);

    private static final LocalDateTime ENRICHED_AT = LocalDateTime.of(2026, 10, 28, 12, 0);

    @Test
    void create_enableItem_whenDisabledIsMissing() {
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", null, NOW);

        assertThat(item)
            .extracting(Item::getName, Item::getRemarks, Item::getBarcode, Item::getUnit, Item::getPictureUrl, Item::isDisabled)
            .containsExactly("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", false);
    }

    @Test
    void create_setCreatedAndUpdatedAtToNow() {
        var item = Item.create("Milk", null, null, null, null, null, NOW);

        assertThat(item)
            .extracting(Item::getCreatedAt, Item::getUpdatedAt)
            .containsExactly(NOW, NOW);
    }

    @Test
    void update_touchUpdatedAtOnly() {
        var item = Item.create("Milk", null, null, null, null, null, NOW);

        item.update("Oat milk", null, null, null, null, null, LATER);

        assertThat(item)
            .extracting(Item::getCreatedAt, Item::getUpdatedAt)
            .containsExactly(NOW, LATER);
    }

    @Test
    void replace_overwriteEveryField() {
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", true, NOW);

        item.replace("Oat milk", null, null, null, null, null, LATER);

        assertThat(item)
            .extracting(Item::getName, Item::getRemarks, Item::getBarcode, Item::getUnit, Item::getPictureUrl, Item::isDisabled, Item::getUpdatedAt)
            .containsExactly("Oat milk", null, null, null, null, false, LATER);
    }

    @Test
    void update_changeOnlyGivenFields() {
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", false, NOW);

        item.update("Oat milk", null, null, null, null, true, NOW);

        assertThat(item)
            .extracting(Item::getName, Item::getRemarks, Item::getBarcode, Item::getUnit, Item::getPictureUrl, Item::isDisabled)
            .containsExactly("Oat milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", true);
    }

    @Test
    void enrich_keepUnit_whenProductHasNone() {
        var item = Item.create("unknown", null, "737628064502", UnitEnum.PCS, null, false, NOW);

        item.enrich("Pasta", "https://example.com/pasta.jpg", null, ENRICHED_AT);

        assertThat(item)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit, Item::getEnrichedAt)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS, ENRICHED_AT);
    }

    @Test
    void enrich_keepNameAndPicture_whenProductHasBlankValues() {
        var item = Item.create("Spaghetti", null, "737628064502", UnitEnum.PCS, "https://example.com/own.jpg", false, NOW);

        item.enrich(" ", null, null, ENRICHED_AT);

        assertThat(item)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit, Item::getEnrichedAt)
            .containsExactly("Spaghetti", "https://example.com/own.jpg", UnitEnum.PCS, ENRICHED_AT);
    }

    @Test
    void markEnrichmentAttempted_setEnrichedAtOnly() {
        var item = Item.create("Spaghetti", null, "000000000000", null, null, false, NOW);

        item.markEnrichmentAttempted(ENRICHED_AT);

        assertThat(item)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit, Item::getEnrichedAt)
            .containsExactly("Spaghetti", null, null, ENRICHED_AT);
    }
}
