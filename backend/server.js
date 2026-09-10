const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = process.env.PORT || 3000;
const DATA_FILE = path.join(__dirname, "data", "anime.json");

function getAnimeData() {
  return JSON.parse(fs.readFileSync(DATA_FILE, "utf8"));
}

const server = http.createServer((req, res) => {
  res.setHeader("Content-Type", "application/json");
  res.setHeader("Access-Control-Allow-Origin", "*");

  const data = getAnimeData();

  if (req.url === "/api/anime") {
    res.writeHead(200);
    res.end(JSON.stringify(data.anime));
    return;
  }

  const detailMatch = req.url.match(/^\/api\/anime\/([^/]+)$/);
  if (detailMatch) {
    const anime = data.anime.find(
      item => item.id === detailMatch[1]
    );

    if (!anime) {
      res.writeHead(404);
      res.end(JSON.stringify({
        error: "Anime not found"
      }));
      return;
    }

    res.writeHead(200);
    res.end(JSON.stringify(anime));
    return;
  }

  const episodeMatch = req.url.match(
    /^\/api\/anime\/([^/]+)\/episodes$/
  );

  if (episodeMatch) {
    const anime = data.anime.find(
      item => item.id === episodeMatch[1]
    );

    if (!anime) {
      res.writeHead(404);
      res.end(JSON.stringify({
        error: "Anime not found"
      }));
      return;
    }

    res.writeHead(200);
    res.end(JSON.stringify(anime.episodes));
    return;
  }

  res.writeHead(404);
  res.end(JSON.stringify({
    error: "Endpoint not found"
  }));
});

server.listen(PORT, () => {
  console.log(`KakaAnime Backend running on port ${PORT}`);
});

const { syncAnime } = require("./sync");

syncAnime();

setInterval(() => {
  syncAnime();
}, 30 * 60 * 1000);
