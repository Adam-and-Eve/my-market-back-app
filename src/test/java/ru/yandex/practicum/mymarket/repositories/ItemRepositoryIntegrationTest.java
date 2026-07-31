package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.ItemModel;

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

        itemRepository.save(item);

        var pageable = PageRequest.of(0, 10);

        var resultPage = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "nOvAtIoN",
                "nOvAtIoN",
                pageable
        );

        Assertions.assertNotNull(resultPage);

        Assertions.assertEquals(1, resultPage.getTotalElements());

        Assertions.assertEquals("Клавиатура Novation Launchkey", resultPage.getContent().getFirst().getTitle());
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

        itemRepository.save(item);

        var pageable = PageRequest.of(0, 10);

        var resultPage = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "МЕХАНИКА",
                "МЕХАНИКА",
                pageable
        );

        Assertions.assertNotNull(resultPage);

        Assertions.assertEquals(1, resultPage.getTotalElements());

        Assertions.assertEquals("Синтезатор", resultPage.getContent().getFirst().getTitle());
    }

    /**
     * <summary>
     * Проверяет, что при отсутствии совпадений как в названии, так и в описании, возвращается пустая страница.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldReturnEmptyPageWhenNoMatchesFound()
    {
        var item = new ItemModel("Гитара", "Акустическая шестиструнная гитара", "/img3.png", 15000L);

        itemRepository.save(item);

        var pageable = PageRequest.of(0, 10);

        var resultPage = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "Барабан",
                "Барабан",
                pageable
        );

        Assertions.assertNotNull(resultPage);

        Assertions.assertTrue(resultPage.getContent().isEmpty());

        Assertions.assertEquals(0, resultPage.getTotalElements());
    }

    /**
     * <summary>
     * Проверяет корректность работы пагинации и сортировки, переданных через объект Pageable.
     * </summary>
     **/
    @Test
    void findByTitleOrDescriptionShouldRespectPageablePaginationAndSorting()
    {
        var cheapKeyboard = new ItemModel("MIDI Клавиатура A", "Базовая модель", "/imgA.png", 10000L);

        var expensiveKeyboard = new ItemModel("MIDI Клавиатура B", "Профессиональная модель", "/imgB.png", 50000L);

        itemRepository.save(cheapKeyboard);

        itemRepository.save(expensiveKeyboard);

        var pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "price"));

        var resultPage = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "MIDI",
                "MIDI",
                pageable
        );

        Assertions.assertNotNull(resultPage);

        Assertions.assertEquals(2, resultPage.getTotalElements());

        Assertions.assertEquals(1, resultPage.getContent().size());

        Assertions.assertEquals("MIDI Клавиатура B", resultPage.getContent().getFirst().getTitle());

        Assertions.assertEquals(50000L, resultPage.getContent().getFirst().getPrice());
    }

    // endregion
}