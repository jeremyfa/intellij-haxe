/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Copyright 2014-2014 TiVo Inc.
 * Copyright 2014-2014 AS3Boyan
 * Copyright 2014-2014 Elias Ku
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.plugins.haxe.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.plugins.haxe.lang.lexer.HaxeTokenTypes;
import com.intellij.plugins.haxe.lang.psi.*;
import com.intellij.plugins.haxe.util.HaxeResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * @author: Srikanth.Ganapavarapu
 */
public abstract class HaxeMethodPsiMixinImpl extends AbstractHaxeNamedComponent implements HaxeMethodPsiMixin {

  private static final Logger LOG = Logger.getInstance("#com.intellij.plugins.haxe.lang.psi.impl.HaxeMethodPsiMixinImpl");
  static {
    LOG.info("Loaded HaxeMethodPsiMixinImpl");
    LOG.setLevel(Level.DEBUG);
  }

  public HaxeMethodPsiMixinImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable @NonNls
  public String getName() {
    String name = super.getName();
    if (null == name) {
      if (getText().contains("function new")) {
        return "new";
      }
      else {
        final PsiIdentifier nameIdentifier = getNameIdentifier();
        if (nameIdentifier != null) {
          name = nameIdentifier.getText();
        }
      }
    }

    return (name != null) ? name : "<unnamed>";
  }


  @Nullable
  @Override
  public List<HaxeDeclarationAttribute> getDeclarationAttributeList() {
    // Not all function types have one of these...  If they do, the
    // subclass (via the generator) will override this method.
    return null;
  }


  @Nullable
  public HaxeReturnStatement getReturnStatement() {
    // Not all function types have one of these...  If they do, the
    // subclass (via the generator) will override this method.
    return findChildByClass(HaxeReturnStatement.class);
  }


  @Nullable
  public HaxeTypeTag getTypeTag() {
    // Not all function types have one of these...  If they do, the
    // subclass (via the generator) will override this method.
    return findChildByClass(HaxeTypeTag.class);
  }


  @Nullable
  @Override
  public PsiType getReturnType() {
    if (isConstructor()) {
      return null;
    }
    // A HaxeFunctionType is a PSI Element.
    // HaxeFunctionType type = getTypeTag().getFunctionType(); // type could be null
    /* TODO: : 'public PsiType getReturnType()': translate above objects into PsiType */
    return null;
  }

  @Nullable
  @Override
  public PsiTypeElement getReturnTypeElement() {
    /* TODO: : 'public PsiType getReturnType()': translate below objects into PsiTypeElement */
    // return getTypeTag();
    return null;
  }


  @Nullable
  public HaxeThrowStatement getThrowStatement() {
    // Not all function types have one of these...  If they do, the
    // subclass (via the generator) will override this method.
    return null;
  }

  @NotNull
  @Override
  public PsiReferenceList getThrowsList() {
    HaxeThrowStatement ts = getThrowStatement();
    return new HaxePsiReferenceList(this.getContainingClass(),
                                    (ts == null ? new HaxeDummyASTNode("ThrowsList") : ts.getNode()),
                                    null);
  }

  @Nullable
  public HaxeBlockStatement getBlockStatement() {
    // Not all function types have one of these...  If they do, the
    // subclass (via the generator) will override this method.
    return null;
  }

  @Nullable
  @Override
  public PsiCodeBlock getBody() {
    HaxeBlockStatement bs = getBlockStatement();
    if (bs == null) {
      return null;
    }

    return (PsiCodeBlock)bs.getNode().getPsi(PsiCodeBlock.class);
  }

  @Override
  public boolean isConstructor() {
    if (super.getName()==null &&
        getComponentName()==null &&
        getText().contains("function new(") &&
        !isStatic() &&
        !isOverride()) {
      return true;
    }
    return false;
  }

  @Nullable
  @Override
  public PsiDocComment getDocComment() {
    // TODO: Fix 'public PsiDocComment getDocComment()'
    //PsiComment psiComment = HaxeResolveUtil.findDocumentation(this);
    //return ((psiComment != null)? new HaxePsiDocComment(this, psiComment) : null);
    return null;
  }

  @Override
  public boolean isVarArgs() {
    // In Haxe, the method is set to VarArgs at runtime, via a function call.
    // We would need the ability to know if a particular run sequence has
    // called such a function.  I don't think we can pull that off without
    // the compiler's help.
    // TODO: Use compiler completion to detect variable arguments usage.
    return false;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public boolean hasTypeParameters() {
    return PsiImplUtil.hasTypeParameters(this);
  }

  @NotNull
  @Override
  public PsiTypeParameter[] getTypeParameters() {
    // Type parameters are those inside of the type designation (e.g.
    // inside the '<' and '>').
    return PsiImplUtil.getTypeParameters(this);
  }

  @Nullable
  @Override
  public PsiClass getContainingClass() {
    return PsiTreeUtil.getParentOfType(this, HaxeClass.class, true);
  }

  @Override
  public PsiElement getContext() {
    final PsiClass cc = getContainingClass();
    return cc != null ? cc : super.getContext();
  }

  @NotNull
  @Override
  public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    // XXX: PsiMethod uses a cache for substitutors.
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }

  @Nullable
  @Override
  public PsiIdentifier getNameIdentifier() {
    PsiIdentifier foundName = null;
    ASTNode node = getNode();
    if (null != node) {
      ASTNode element = node.findChildByType(HaxeTokenTypes.IDENTIFIER);
      if (null != element) {
        foundName = (PsiIdentifier) element.getPsi();
      }
    }
    return foundName;
  }

  @NotNull
  @Override
  public PsiMethod[] findSuperMethods() {
    return HaxeMethodUtils.findSuperMethods(this);
  }

  @NotNull
  @Override
  public PsiMethod[] findSuperMethods(boolean checkAccess) {
    return HaxeMethodUtils.findSuperMethods(this);
  }

  @NotNull
  @Override
  public PsiMethod[] findSuperMethods(PsiClass parentClass) {
    return HaxeMethodUtils.findSuperMethods(this, parentClass);
  }

  @NotNull
  @Override
  public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    return HaxeMethodUtils.findSuperMethodSignaturesIncludingStatic(this);
  }

  @Deprecated
  @Nullable
  @Override
  public PsiMethod findDeepestSuperMethod() {
    return HaxeMethodUtils.findDeepestSuperMethod(this);
  }

  @NotNull
  @Override
  public PsiMethod[] findDeepestSuperMethods() {
    return HaxeMethodUtils.findDeepestSuperMethods(this);
  }

  @Nullable
  @Override
  public PsiTypeParameterList getTypeParameterList() {
    // Type parameters are those inside of the type designation (e.g.
    // inside the '<' and '>').
    HaxeTypeTag         tag =   (HaxeTypeTag) findChildByType(HaxeTokenTypes.TYPE_TAG);
    HaxeTypeOrAnonymous toa =   null == tag ? null : tag.getTypeOrAnonymous();
    HaxeType            type =  null == toa ? null : toa.getType();
    HaxeTypeParam       param = null == type ? null : type.getTypeParam();// XXX: Java<->Haxe list & type inversion -- See BNF.
    return param;
  }

  private boolean isPrivate() {
    final List<HaxeDeclarationAttribute> declarationAttributeList = getDeclarationAttributeList();
    if (declarationAttributeList != null) {
      for (HaxeDeclarationAttribute declarationAttribute : declarationAttributeList) {
        HaxeAccess access = declarationAttribute.getAccess();
        if (access!=null && "private".equals(access.getText())) {
            return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isPublic() {
    return (!isPrivate() && super.isPublic()); // do not change the order of- and the- expressions
  }

  @NotNull
  @Override
  public HaxeModifierList getModifierList() {

    //
    // Note Haxe's rules for visibility:
    // (from http://haxe.org/manual/class-field-visibility.html)
    //
    // Omitting the visibility modifier usually defaults the visibility to private,
    // but there are exceptions where it becomes public instead:
    //
    // - If the class is declared as extern.
    // - If the field id declared on an interface.
    // - If the field overrides a public field.
    //
    // Trivia: Protected
    //
    //   Haxe has no notion of a protected keyword known from Java, C++ and
    //   other object-oriented languages. However, its private behavior is
    //   equal to those language's protected behavior, so Haxe actually
    //   lacks their real private behavior.
    //

    HaxeModifierList list = super.getModifierList();

    if (null == list) {
      list = new HaxeModifierListImpl(this.getNode());
    }

    // -- below modifiers need to be set individually
    //    because, they cannot be enforced through macro-list

    if (isStatic()) {
      list.setModifierProperty(HaxePsiModifier.STATIC, true);
    }

    if (isPublic()) {
      list.setModifierProperty(HaxePsiModifier.PUBLIC, true);
    }
    else {
      list.setModifierProperty(HaxePsiModifier.PRIVATE, true);
    }

    return list;
  }

  @Override
  public boolean hasModifierProperty(@HaxePsiModifier.ModifierConstant @NonNls @NotNull String name) {
    return this.getModifierList().hasModifierProperty(name);
  }

  @NotNull
  @Override
  public HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }

  @NotNull
  @Override
  public PsiParameterList getParameterList() {
    final HaxeParameterList list = PsiTreeUtil.getChildOfType(this, HaxeParameterList.class);
    return ((list != null) ? list : new HaxeParameterListImpl(new HaxeDummyASTNode("Dummy parameter list")));
  }
}
