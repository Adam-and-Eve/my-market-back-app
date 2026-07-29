package ru.yandex.practicum.mymarket.configurations;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.models.ItemModel;

import java.util.List;

/**
 * <summary>
 * Класс конфигурации для первоначальной инициализации данных каталога.
 * Выполняет первичное наполнение базы данных демонстрационным набором товаров при первом запуске приложения.
 * </summary>
 **/
@Configuration
public class CatalogDataInitializerConfiguration {

    /**
     * <summary>
     * Создает бин CommandLineRunner, проверяющий наполненность базы данных товаров при старте приложения.
     * Если хранилище пусто, сохраняет базовый ассортимент товаров в репозиторий.
     * </summary>
     * @param itemRepository Репозиторий для управления сущностями товаров в базе данных.
     * <return>
     * @return Экземпляр функционального интерфейса CommandLineRunner с логикой инициализации.
     * </return>
     **/
    @Bean
    CommandLineRunner initializeCatalog(final ItemRepository itemRepository) {
        return args -> {
            if (itemRepository.count() > 0){
                return;
            }

            itemRepository.saveAll(List.of(
                    new ItemModel(
                            "Ноутбук ASUS ROG Strix SCAR 18",
                            "Топовый игровой ноутбук на базе процессора Intel Core Ultra 9 и видеокарты NVIDIA RTX 4090. Экран 18 дюймов Nebula HDR ROG Mini LED 240Hz.",
                            "images/rog_scar18.png",
                            385000
                    ),
                    new ItemModel(
                            "Клавиатура Keychron Q1 Pro",
                            "Кастомная механическая клавиатура в цельноалюминиевом корпусе. Поддержка QMK/VIA, горячая замена переключателей (Hot-Swap), беспроводное подключение.",
                            "images/keychron_q1.png",
                            22500
                    ),
                    new ItemModel(
                            "Мышь Logitech G Pro X Superlight 2",
                            "Сверхлегкая беспроводная игровая мышь весом всего 60 грамм. Гибридные переключатели LIGHTFORCE, сенсор HERO 2 с частотой опроса до 2000 Гц.",
                            "images/logitech_mouse.png",
                            16800
                    ),
                    new ItemModel(
                            "Процессор Intel Core Ultra 9 285K",
                            "Флагманский десктопный процессор архитектуры Arrow Lake. 24 ядра (8 P-ядер и 16 E-ядер), базовая частота 3.7 ГГц, разблокированный множитель.",
                            "images/intel_u9.png",
                            65000
                    )
            ));
        };
    }
}