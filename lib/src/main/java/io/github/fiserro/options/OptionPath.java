package io.github.fiserro.options;

import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class OptionPath implements Path {

  private final List<Node> nodes;

  public static OptionPath empty() {
    return new OptionPath(List.of());
  }

  public OptionPath add(String propertyName) {
    val nodesPlus = Stream.concat(nodes.stream(), Stream.of(new OptionNode(propertyName)))
        .toList();
    return new OptionPath(nodesPlus);
  }

  @Override
  public String toString() {
    return nodes.stream().map(Node::toString).collect(Collectors.joining("/"));
  }

  @Override
  public Iterator<Node> iterator() {
    return nodes.iterator();
  }

  @Override
  public void forEach(Consumer<? super Node> action) {
    nodes.forEach(action);
  }

  @Override
  public Spliterator<Node> spliterator() {
    return nodes.spliterator();
  }

  public record OptionNode(String name) implements Node {

    @Override
    public String getName() {
      return name();
    }

    @Override
    public boolean isInIterable() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Integer getIndex() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object getKey() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ElementKind getKind() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Node> T as(Class<T> aClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
