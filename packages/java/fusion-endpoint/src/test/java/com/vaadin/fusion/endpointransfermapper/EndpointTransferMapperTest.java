package com.vaadin.fusion.endpointransfermapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vaadin.fusion.mappedtypes.Order;
import com.vaadin.fusion.mappedtypes.Pageable;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
        Assert.assertEquals(List.class,
                endpointTransferMapper.getTransferType(Page.class));
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
        Assert.assertEquals(List.class.getName(),
                endpointTransferMapper.getTransferType(Page.class.getName()));
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
    public void page_toTransferType() {
        List<String> content = new ArrayList<>();
        content.add("First");
        content.add("Second");

        Page p = new PageImpl<>(content);
        List l = (List) endpointTransferMapper.toTransferType(p);
        Assert.assertEquals(content, l);
    }

    @Test
    public void page_fromTransferType() {
        List<String> incoming = new ArrayList<>();
        incoming.add("First");
        incoming.add("Second");
        Page p = endpointTransferMapper.toEndpointType(incoming, Page.class);
        Assert.assertEquals(incoming, p.getContent());
    }
}
