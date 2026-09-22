package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/cards", layout = MainLayout.class)
@PageTitle("Cards")
public class ListCardsView extends VerticalLayout {

    final Grid<Card> grid = new Grid<>(Card.class, false);
    final TextField filterField = new TextField();

    public ListCardsView(ListCardsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(Card::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Card::getOwner).setHeader("Owner").setSortable(true);
        grid.addColumn(card -> card.getCardType() != null ? MessageSource.get(card.getCardType().toString()) : "")
                .setHeader("Type").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(card -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormCardView.class,
                            new RouteParameters("id", String.valueOf(card.getId()))));

            var statsBtn = new Button("Statistics");
            statsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            statsBtn.addClickListener(e ->
                    UI.getCurrent().navigate(CardStatisticsView.class,
                            new RouteParameters("id", String.valueOf(card.getId()))));

            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailCardView.class,
                            new RouteParameters("id", String.valueOf(card.getId()))));

            return new HorizontalLayout(editBtn, statsBtn, deleteBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
                query -> presenter.findAll(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        null,
                        query.getOffset(),
                        query.getPageSize()).getContent().stream(),
                query -> presenter.count(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        null)
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormCardView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
