-- Atlas EMS Sample Data
-- Uses ON CONFLICT DO NOTHING so restarts do not cause duplicate key errors.

-- Categories
INSERT INTO categories (id, name, description)
VALUES
  (1, 'Sound Equipment', 'Speakers, amplifiers, mixers, and subwoofers'),
  (2, 'Lighting',        'Stage lights, LED bars, spotlights, and strobes'),
  (3, 'Cables',          'Power cables, XLR cables, and signal cables')
ON CONFLICT (id) DO NOTHING;

-- Keep the id sequence in sync after manual inserts
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- Equipment
INSERT INTO equipment (id, name, description, category_id, total_quantity, available_quantity, status)
VALUES
  (1, 'JBL SRX835P Speaker',   'Powered 15-inch 3-way speaker, 2000W',         1, 10, 10, 'IN_STOCK'),
  (2, 'Yamaha MG16 Mixer',     '16-channel analog mixing console',               1,  4,  4, 'IN_STOCK'),
  (3, 'Crown XTi 2002 Amp',    '2-channel power amplifier, 800W per channel',    1,  6,  6, 'IN_STOCK'),
  (4, 'Chauvet Intimidator',   'Moving head LED beam light, DMX-controlled',     2,  8,  8, 'IN_STOCK'),
  (5, 'XLR Cable 10m',         'Balanced XLR microphone and line cable, 10m',    3, 30, 30, 'IN_STOCK')
ON CONFLICT (id) DO NOTHING;

SELECT setval('equipment_id_seq', (SELECT MAX(id) FROM equipment));

-- Events
INSERT INTO events (id, name, venue, event_date, description, status)
VALUES
  (1, 'Kigali Business Summit 2025',  'Kigali Convention Centre', '2025-07-15', 'Annual corporate networking summit', 'PLANNED'),
  (2, 'Wedding — Nzeyimana Family',   'Heaven Restaurant, Kigali', '2025-08-02', 'Reception and live music setup',     'PLANNED')
ON CONFLICT (id) DO NOTHING;

SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
