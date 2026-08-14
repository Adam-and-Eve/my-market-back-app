MERGE INTO items (id, title, description, img_path, price) KEY (id) VALUES
    (1, 'Ноутбук ASUS ROG Strix SCAR 18', 'Топовый игровой ноутбук на базе процессора Intel Core Ultra 9 и видеокарты NVIDIA RTX 4090. Экран 18 дюймов Nebula HDR ROG Mini LED 240Hz.', 'images/rog_scar18.png', 385000),
    (2, 'Клавиатура Keychron Q1 Pro', 'Кастомная механическая клавиатура в цельноалюминиевом корпусе. Поддержка QMK/VIA, горячая замена переключателей (Hot-Swap), беспроводное подключение.', 'images/keychron_q1.png', 22500),
    (3, 'Мышь Logitech G Pro X Superlight 2', 'Сверхлегкая беспроводная игровая мышь весом всего 60 грамм. Гибридные переключатели LIGHTFORCE, сенсор HERO 2 с частотой опроса до 2000 Гц.', 'images/logitech_mouse.png', 16800),
    (4, 'Процессор Intel Core Ultra 9 285K', 'Флагманский десктопный процессор архитектуры Arrow Lake. 24 ядра (8 P-ядер и 16 E-ядер), базовая частота 3.7 ГГц, разблокированный множитель.', 'images/intel_u9.png', 65000);

ALTER TABLE items ALTER COLUMN id RESTART WITH 5;

MERGE INTO users (id, username, enabled) KEY (id) VALUES
    (1, 'admin', TRUE),
    (2, 'user', TRUE);

ALTER TABLE users ALTER COLUMN id RESTART WITH 3;