package dev.mmatokovic.pantrio.domain.item;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemShould {

    @Test
    void create_enableItem_whenDisabledIsMissing() {
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", null);

        assertThat(item)
            .extracting(Item::getName, Item::getRemarks, Item::getBarcode, Item::getUnit, Item::getPictureUrl, Item::isDisabled)
            .containsExactly("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", false);
    }

    @Test
    void update_changeOnlyGivenFields() {
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", false);

        item.update("Oat milk", null, null, null, null, true);

        assertThat(item)
            .extracting(Item::getName, Item::getRemarks, Item::getBarcode, Item::getUnit, Item::getPictureUrl, Item::isDisabled)
            .containsExactly("Oat milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", true);
    }

    @Test
    void enrich_keepUnit_whenProductHasNone() {
        var item = Item.create("unknown", null, "737628064502", UnitEnum.PCS, null, false);

        item.enrich("Pasta", "https://example.com/pasta.jpg", null);

        assertThat(item)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS);
    }
}
