package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.ItemModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения кастомных запросов в ItemRepository.
 * Использует легковесный срез контекста для проверки слоя доступа к данным.
 * </summary>
 **/
public class ItemRepositoryIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @Autowired
    private ItemRepository itemRepository;

    // endregion

    // region Setup

    @BeforeEach
    void clear() {
        itemRepository.deleteAll().block();
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет поиск товара по подстроке в наименовании без учета регистра символов.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldFindItemWhenTitleMatchesCaseInsensitive()
    {
        var item = new ItemModel("Клавиатура Novation Launchkey", "Инструмент для студии", "/img1.png", 45000L);

        itemRepository.save(item).block();

        var pageable = PageRequest.of(0, 10);

        var resultList = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "nOvAtIoN",
                "nOvAtIoN",
                pageable
        ).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertEquals(1, resultList.size());

        Assertions.assertEquals("Клавиатура Novation Launchkey", resultList.getFirst().getTitle());
    }

    /**
     * <summary>
     * Проверяет поиск товара по подстроке в описании без учета регистра символов.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldFindItemWhenDescriptionMatchesCaseInsensitive()
    {
        var item = new ItemModel("Синтезатор", "Полувзвешенная механика клавиш", "/img2.png", 60000L);

        itemRepository.save(item).block();

        var pageable = PageRequest.of(0, 10);

        var resultList = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "МЕХАНИКА",
                "МЕХАНИКА",
                pageable
        ).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertEquals(1, resultList.size());

        Assertions.assertEquals("Синтезатор", resultList.getFirst().getTitle());
    }

    /**
     * <summary>
     * Проверяет, что при отсутствии совпадений как в названии, так и в описании, возвращается пустой поток.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldReturnEmptyListWhenNoMatchesFound()
    {
        var item = new ItemModel("Гитара", "Акустическая шестиструнная гитара", "/img3.png", 15000L);

        itemRepository.save(item).block();

        var pageable = PageRequest.of(0, 10);

        var resultList = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "Барабан",
                "Барабан",
                pageable
        ).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertTrue(resultList.isEmpty());
    }

    /**
     * <summary>
     * Проверяет корректность работы пагинации и сортировки элементов реактивного потока Flux, переданных через объект Pageable.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldRespectPageablePaginationAndSorting()
    {
        var cheapKeyboard = new ItemModel("MIDI Клавиатура A", "Базовая модель", "/imgA.png", 10000L);

        var expensiveKeyboard = new ItemModel("MIDI Клавиатура B", "Профессиональная модель", "/imgB.png", 50000L);

        itemRepository.saveAll(List.of(cheapKeyboard, expensiveKeyboard)).collectList().block();

        var pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "price"));

        var resultList = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "MIDI",
                "MIDI",
                pageable
        ).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertEquals(1, resultList.size());

        Assertions.assertEquals("MIDI Клавиатура B", resultList.getFirst().getTitle());

        Assertions.assertEquals(50000L, resultList.getFirst().getPrice());
    }

    // endregion
}