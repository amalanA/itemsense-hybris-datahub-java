package com.impinj.datahub.composition.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hybris.datahub.composition.CompositionRuleHandler;
import com.hybris.datahub.composition.impl.AbstractCompositionRuleHandler;
import com.hybris.datahub.domain.CanonicalAttributeDefinition;
import com.hybris.datahub.model.CanonicalItem;
import com.hybris.datahub.model.CompositionGroup;
import com.hybris.datahub.model.RawItem;

/**
 * @deprecated not used composition for impinj.
 */
@Deprecated
public class ImpinjStockCompositionHandler extends AbstractCompositionRuleHandler implements CompositionRuleHandler
{
	private static final String CANONICAL_TYPE = "CanonicalImpinjStocktem";
	private static final String CANONICAL_PRODUCT_ID = "productId";
	private static final String CANONICAL_STOCK_LEVEL = "stockLevel";
	private static final String RAW_PRODUCT_ID = "productId";
	private static final String RAW_STOCK_LEVEL = "stockLevel";

	@Override
	public <T extends CanonicalItem> T compose(final CanonicalAttributeDefinition canonicalAttributeDefinition,
			final CompositionGroup<? extends RawItem> compositionGroup, final T canonicalItem)
	{
		assert compositionGroup != null;
		assert canonicalItem != null;
		assert compositionGroup.getItems() != null;
		final String productId = findFirstPopulatedValue(compositionGroup.getItems(), RAW_PRODUCT_ID);
		canonicalItem.setField(CANONICAL_PRODUCT_ID, productId);
		final Integer stockLevel = Integer.parseInt(findFirstPopulatedValue(compositionGroup.getItems(), RAW_STOCK_LEVEL));
		canonicalItem.setField(CANONICAL_STOCK_LEVEL, stockLevel);
		return canonicalItem;
	}

	@Override
	public boolean isApplicable(final CanonicalAttributeDefinition attribute)
	{
		return attribute.getCanonicalAttributeModelDefinition().getAttributeName().equals(CANONICAL_TYPE)
				&& attribute.getCanonicalAttributeModelDefinition().getCanonicalItemMetadata().getItemType().equals(CANONICAL_TYPE);
	}

	private String findFirstPopulatedValue(final List<? extends RawItem> rawItemList, final String attributeName)
	{
		for (final RawItem customer : rawItemList)
		{
			final String value = (String) customer.getField(attributeName);
			if (StringUtils.isNotBlank(value))
			{
				return value;
			}
		}
		return null;
	}

}
