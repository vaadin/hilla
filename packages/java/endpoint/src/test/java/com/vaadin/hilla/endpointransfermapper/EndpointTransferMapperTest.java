package com.vaadin.hilla.endpointransfermapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vaadin.hilla.mappedtypes.Order;
import com.vaadin.hilla.mappedtypes.Pageable;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

public class EndpointTransferMapperTest {

    private EndpointTransferMapper endpointTransferMapper = new EndpointTransferMapper();

    @Test
    public void getTransferTypeClass_works() {
        Assert.assertEquals(Pageable.class,
                endpointTransferMapper.getTransferType(
                        org.springframework.data.domain.Pageable.class));
        Assert.assertEquals(Pageable.class, endpointTransferMapper
                .getTransferType(AbstractPageRequest.class));
        Assert.assertEquals(Pageable.class,
                endpointTransferMapper.getTransferType(PageRequest.class));
        Assert.assertEquals(com.vaadin.hilla.mappedtypes.Page.class,
                endpointTransferMapper.getTransferType(Page.class));
        Assert.assertEquals(com.vaadin.hilla.mappedtypes.Slice.class,
                endpointTransferMapper.getTransferType(Slice.class));
        Assert.assertEquals(String.class,
                endpointTransferMapper.getTransferType(UUID.class));
        Assert.assertNull(
                endpointTransferMapper.getTransferType(Integer.class));
    }

    @Test
    public void getTransferTypeString_works() {
        Assert.assertEquals(Pageable.class.getName(),
                endpointTransferMapper.getTransferType(
                        org.springframework.data.domain.Pageable.class
                                .getName()));
        // When defining the methods, the defined classes are used to we do not
        // need to
        // support e.g. AbstractPageRequest and PageRequest here
        Assert.assertEquals(com.vaadin.hilla.mappedtypes.Page.class.getName(),
                endpointTransferMapper.getTransferType(Page.class.getName()));
        Assert.assertEquals(com.vaadin.hilla.mappedtypes.Slice.class.getName(),
                endpointTransferMapper.getTransferType(Slice.class.getName()));
        Assert.assertEquals(String.class.getName(),
                endpointTransferMapper.getTransferType(UUID.class.getName()));
        Assert.assertNull(endpointTransferMapper
                .getTransferType(Integer.class.getName()));
    }

    @Test
    public void integer_not_mapped() {
        Integer i = new Integer(123);
        Assert.assertSame(i, endpointTransferMapper.toTransferType(i));
        Assert.assertSame(i,
                endpointTransferMapper.toEndpointType(i, Integer.class));
    }

    @Test
    public void pageable_simple_toEndpointType() {
        Pageable dto = new Pageable();
        dto.setPageNumber(1);
        dto.setPageSize(2);

        org.springframework.data.domain.Pageable pageable = endpointTransferMapper
                .toEndpointType(dto,
                        org.springframework.data.domain.Pageable.class);

        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(2, pageable.getPageSize());
        Assert.assertTrue(pageable.getSort().isUnsorted());
    }

    @Test
    public void pageable_sortorder_toEndpointType() {
        Pageable dto = new Pageable();
        dto.setPageNumber(1);
        dto.setPageSize(2);

        Order order1 = new Order();
        order1.setDirection(Direction.ASC);
        order1.setProperty("p1");
        Order order2 = new Order();
        order2.setDirection(Direction.DESC);
        order2.setIgnoreCase(true);
        order2.setNullHandling(NullHandling.NULLS_LAST);
        order2.setProperty("p2");

        dto.getSort().getOrders().add(order1);
        dto.getSort().getOrders().add(order2);
        org.springframework.data.domain.Pageable pageable = endpointTransferMapper
                .toEndpointType(dto,
                        org.springframework.data.domain.Pageable.class);

        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(2, pageable.getPageSize());

        Assert.assertFalse(pageable.getSort().isUnsorted());
        Assert.assertEquals("p1",
                pageable.getSort().toList().get(0).getProperty());
        Assert.assertEquals("p2",
                pageable.getSort().toList().get(1).getProperty());

        org.springframework.data.domain.Sort.Order p1Order = pageable.getSort()
                .getOrderFor("p1");
        org.springframework.data.domain.Sort.Order p2Order = pageable.getSort()
                .getOrderFor("p2");

        Assert.assertEquals(Direction.ASC, p1Order.getDirection());
        Assert.assertEquals(Direction.DESC, p2Order.getDirection());
        Assert.assertFalse(p1Order.isIgnoreCase());
        Assert.assertTrue(p2Order.isIgnoreCase());
        Assert.assertEquals(null, p1Order.getNullHandling());
        Assert.assertEquals(NullHandling.NULLS_LAST, p2Order.getNullHandling());
    }

    @Test
    public void pageable_toTransferType() {
        org.springframework.data.domain.Pageable p = org.springframework.data.domain.Pageable
                .ofSize(10);
        Pageable dto = (Pageable) endpointTransferMapper.toTransferType(p);
        Assert.assertEquals(10, dto.getPageSize());
    }

    @Test
    public void uuid_toTransferType() {
        UUID u = new UUID(123112312323L, 351231236356365L);
        String s = (String) endpointTransferMapper.toTransferType(u);
        Assert.assertEquals("0000001c-aa10-ce03-0001-3f716513b90d", s);
    }

    @Test
    public void uuid_fromTransferType() {
        String s = "0000001c-aa10-ce03-0001-3f716513b90d";
        UUID u = endpointTransferMapper.toEndpointType(s, UUID.class);
        Assert.assertEquals("0000001c-aa10-ce03-0001-3f716513b90d",
                u.toString());
    }

    @Test
    public void page_toTransferType_returnsMappedPage() {
        List<String> content = new ArrayList<>();
        content.add("First");
        content.add("Second");
        content.add("Third");

        org.springframework.data.domain.Pageable pageable = PageRequest.of(1,
                3);
        Page<String> p = new PageImpl<>(content, pageable, 10);

        com.vaadin.hilla.mappedtypes.Page<?> mappedPage = (com.vaadin.hilla.mappedtypes.Page<?>) endpointTransferMapper
                .toTransferType(p);

        Assert.assertEquals(content, mappedPage.getContent());
        Assert.assertEquals(3, mappedPage.getNumberOfElements());
        Assert.assertEquals(1, mappedPage.getNumber());
        Assert.assertEquals(3, mappedPage.getSize());
        Assert.assertFalse(mappedPage.isFirst());
        Assert.assertFalse(mappedPage.isLast());
        Assert.assertTrue(mappedPage.isHasNext());
        Assert.assertTrue(mappedPage.isHasPrevious());
        Assert.assertTrue(mappedPage.isHasContent());
        Assert.assertFalse(mappedPage.isEmpty());
        Assert.assertEquals(10, mappedPage.getTotalElements());
        Assert.assertEquals(4, mappedPage.getTotalPages());
    }

    @Test
    public void page_fromTransferType_throwsUnsupportedOperation() {
        com.vaadin.hilla.mappedtypes.Page<String> mappedPage = new com.vaadin.hilla.mappedtypes.Page<>();
        List<String> content = new ArrayList<>();
        content.add("First");
        content.add("Second");
        mappedPage.setContent(content);

        try {
            endpointTransferMapper.toEndpointType(mappedPage, Page.class);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Page is unidirectional
            Assert.assertTrue(e.getMessage()
                    .contains("Cannot create a Page from a transfer Page"));
        }
    }

    @Test
    public void slice_toTransferType_returnsMappedSlice() {
        List<String> content = new ArrayList<>();
        content.add("First");
        content.add("Second");
        content.add("Third");

        // Create a middle slice that has next but not previous
        org.springframework.data.domain.Pageable pageable = PageRequest.of(1,
                3);
        Slice<String> s = new SliceImpl<>(content, pageable, true);

        com.vaadin.hilla.mappedtypes.Slice<?> mappedSlice = (com.vaadin.hilla.mappedtypes.Slice<?>) endpointTransferMapper
                .toTransferType(s);

        Assert.assertEquals(content, mappedSlice.getContent());
        Assert.assertEquals(3, mappedSlice.getNumberOfElements());
        Assert.assertEquals(1, mappedSlice.getNumber());
        Assert.assertEquals(3, mappedSlice.getSize());
        Assert.assertFalse(mappedSlice.isFirst());
        Assert.assertFalse(mappedSlice.isLast());
        Assert.assertTrue(mappedSlice.isHasNext());
        Assert.assertTrue(mappedSlice.isHasPrevious());
        Assert.assertTrue(mappedSlice.isHasContent());
        Assert.assertFalse(mappedSlice.isEmpty());
    }

    @Test
    public void slice_fromTransferType_throwsUnsupportedOperation() {
        com.vaadin.hilla.mappedtypes.Slice<String> mappedSlice = new com.vaadin.hilla.mappedtypes.Slice<>();
        List<String> content = new ArrayList<>();
        content.add("First");
        content.add("Second");
        mappedSlice.setContent(content);

        try {
            endpointTransferMapper.toEndpointType(mappedSlice, Slice.class);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Slice is unidirectional
            Assert.assertTrue(e.getMessage()
                    .contains("Cannot create a Slice from a transfer Slice"));
        }
    }
}
