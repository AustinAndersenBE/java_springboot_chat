Module-Oriented Programming

Code should be written in units called modules.

Code should be written in state modules and logic modules.
State modules may contain internal state and may reach out for external state
Logic modules contain no internal state nor reach out for external state

logic modules only consist of functions and these functions only touch stateful things which are explicitly passed to them. So a logic function can read and write to a file if an open file is passed into it but a logic function cannot itself open any files.

Management of state is not the responsibility of logic code. A logic function may generate new data and may mutate it's inputs but its only responsibility is to generate and mutate data only as it says it will in its documentation. The side effects of a logic function are the responsible of its callers. So the relationship between state modules and logic modules are strictly one way. State code can call logic code but not the other way around.

As for the state modules, each should protect it's private state and state modules should only directly touch each other's public interfaces. In other words, the modules should be encapsulated. 

public interface
private implementation/state (but do this in a much coarser-grained encapsulation than OOP)

And even though logic modules have no state to protect, they too should distinguish between public functions and private functions to minimize exposed surface area. 
This is different from OOP in a way because there is no rule stating how big modules should get. They might be tens of thousands of lines long. Very large modules are perhaps not ideal but no hard rules. Second, a module is not an instance of a data type. In almost all cases, our state modules are singletons and we are unapologetic about it. 

Data types don't belong anywhere. They are a stand alone. They live outside all modules and when data is transmitted from one module to another. The structure of that data belongs to neither module more than the other. As a practical matter though, data type must be defined somewhere in code. So we generally define it in the module that's most prodominantly used. We may also sometimes want to encapsulate operations on a datatype in which case we put the data type and those functions in the same module.

Because state management is an ugly problem, the general goal is to minimize the proportion of state code. As much as possible we want to punt code from our state modules into logic modules. 

For state modules, a major reason to break them up is to divide and conquer state management. For other modules, we might break them up for organization. The modules might only be coupled through the public interfaces. Even for logic modules, this can be helpful because it allows one group to change an internal implementation as long as the public interface remains unchanged. For similar reasons, we might split them to better formalize an externally exposd API. We dont want to bother out external users with details that don't concern them and we want the freedom to change what we've kept private again. These ideas predate OOP and it doesn't require us to follow the rest of it's prescriptions. We should not conflate modules with datatypes and we should not obsessively wittle the modules down to tiny sizes. 

Interface types are useful for module interop and API's. The utility is primarily across module boundaries, particularly across boundaries between an API and it's consumer. With these interface types we can formalize commonalities between different data types including datatypes defined externally to our own code. Much like a protocol allows us to treat clients servers and peers like swappable components, interfaces allow us to interoperate with code that hasn't yet been written. If however, I have no need to allow for such extensibility, I generally avoid interfaces because it requires me to speculatively generalize and to imagine needs I don't concrentely yet have and this burden does not always pay off.

For internal business, for modules well contained within my control, I don't really care about that kind of extensibility. For code I control, flexibility is maintained by favoring the simple solutions to my concrete problems rather than enter in the realm of speculation. 

Do not use inheritance for 99% cases because it causes alot of problems unless you have a good reason to.

The problems with OOP is that it conflates modules with data types and it is overly optimistic about useful abstractions. and it favors design with too many small pieces.