package dev.sbs.api.collection.query;

import dev.sbs.api.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the direction of a sort operation, either ascending or descending.
 * Each constant carries both a short abbreviation and a fully capitalized display name.
 */
@RequiredArgsConstructor
public enum SortOrder {

	/** Ascending sort order (smallest to largest). */
	ASCENDING("ASC"),
	/** Descending sort order (largest to smallest). */
	DESCENDING("DESC");

	@Getter private final @NotNull String shortName;

	/**
	 * Returns the fully capitalized display name of this sort order (e.g., "Ascending").
	 *
	 * @return the human-readable name
	 */
	public String getName() {
		return StringUtil.capitalizeFully(this.name());
	}

}
