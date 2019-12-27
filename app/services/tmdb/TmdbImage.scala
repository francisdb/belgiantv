package services.tmdb

case class TmdbImage(
  id: String,
  `type`: String,
  size: String,
  height: Int,
  width: Int,
  url: String
) {}
