const fs = require("fs");
const path = require("path");

const DATA_FILE = path.join(__dirname, "data", "anime.json");

function loadDatabase() {
  return JSON.parse(fs.readFileSync(DATA_FILE, "utf8"));
}

function saveDatabase(data) {
  fs.writeFileSync(
    DATA_FILE,
    JSON.stringify(data, null, 2) + "\n"
  );
}

async function syncAnime() {
  console.log(`[SYNC] Memulai sinkronisasi: ${new Date().toISOString()}`);

  const database = loadDatabase();

  // Source provider akan dipasang di sini.
  // Untuk sekarang kita hanya memastikan database bisa dibaca.
  console.log(`[SYNC] Anime dalam database: ${database.anime.length}`);

  saveDatabase(database);

  console.log("[SYNC] Sinkronisasi selesai.");
}

module.exports = {
  syncAnime
};
