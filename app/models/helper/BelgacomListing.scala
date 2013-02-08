package models.helper

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomListing(
    tvmovies:BelgacomResult,
    tvgrid :BelgacomResult) {
}