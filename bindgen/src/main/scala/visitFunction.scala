package bindgen

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scalanative.libc.*
import libclang.defs.*
import libclang.types.*
import libclang.enumerations.*
import scala.collection.mutable.ListBuffer

def visitFunction(functionCursor: CXCursor)(using Zone) =
  val mem = stackalloc[Def.Function](1)

  zone {
    val typ = clang_getCursorType(functionCursor)
    val functionName = clang_getCursorSpelling(functionCursor).string
    val returnType = clang_getResultType(typ)

    !mem = Def.Function(
      functionName,
      constructType(returnType),
      ListBuffer.empty,
      tpe = CFunctionType.Extern
    )

    clang_visitChildren(
      functionCursor,
      CXCursorVisitor { (cursor: CXCursor, parent: CXCursor, d: CXClientData) =>
        zone {
          val builder = (!d.unwrap[Def.Function])
          if cursor.kind == CXCursorKind.CXCursor_ParmDecl then
            val parameterName = clang_getCursorSpelling(cursor).string
            val parameterType = constructType(clang_getCursorType(cursor))

            builder.parameters.addOne(parameterName -> parameterType)
            CXChildVisitResult.CXChildVisit_Continue
          else CXChildVisitResult.CXChildVisit_Recurse
          end if
        }
      },
      CXClientData.wrap(mem)
    )
  }
  !mem
end visitFunction
